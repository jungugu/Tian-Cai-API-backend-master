package com.nuomi.tianCaiAPI.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayEbppPdeductAsyncPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.config.AliPayAccountConfig;
import com.nuomi.tianCaiAPI.config.EmailConfig;
import com.nuomi.tianCaiAPI.constant.CommonConstant;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.mapper.ProductOrderMapper;
import com.nuomi.tianCaiAPI.model.dto.post.PostQueryRequest;
import com.nuomi.tianCaiAPI.model.dto.pruductOrder.ProductOrderQueryRequest;
import com.nuomi.tianCaiAPI.model.entity.*;
import com.nuomi.tianCaiAPI.model.enums.ProductOrderStatusEnum;
import com.nuomi.tianCaiAPI.model.vo.AliPayAsyncResponse;
import com.nuomi.tianCaiAPI.model.vo.PaymentInfoVO;
import com.nuomi.tianCaiAPI.model.vo.ProductOrderVO;
import com.nuomi.tianCaiAPI.model.vo.UserVO;
import com.nuomi.tianCaiAPI.service.*;
import com.nuomi.tianCaiAPI.utils.EmailUtil;
import com.nuomi.tianCaiAPI.utils.RedissonLockUtil;
import com.nuomi.tianCaiAPI.utils.SqlUtils;
import com.tiancaiapi.common.entity.User;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.nuomi.tianCaiAPI.constant.PayConstant.*;
import static com.nuomi.tianCaiAPI.model.enums.OrderPayTypeEnum.ZFB;
import static com.nuomi.tianCaiAPI.model.enums.ProductOrderStatusEnum.*;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.title;

/**
 * @author NuoMi
 */
@Service
@Slf4j
public class AlipayOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private AliPayAccountConfig aliPayAccountConfig;

    @Resource
    private UserService userService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private RechargeRecordService rechargeRecordService;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private EmailConfig emailConfig;

    @Resource
    private RedissonLockUtil redissonLockUtil;


    /**
     * 获取查询包装类
     *
     * @param productOrderQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ProductOrder> getQueryWrapper(ProductOrderQueryRequest productOrderQueryRequest) {
        QueryWrapper<ProductOrder> queryWrapper = new QueryWrapper<>();
        if (productOrderQueryRequest == null) {
            return queryWrapper;
        }
        Long id = productOrderQueryRequest.getId();
        Long userId = productOrderQueryRequest.getUserId();
        Long productId = productOrderQueryRequest.getProductId();
        String orderNo = productOrderQueryRequest.getOrderNo();
        String orderName = productOrderQueryRequest.getOrderName();
        String status = productOrderQueryRequest.getStatus();
        String payType = productOrderQueryRequest.getPayType();
        String sortField = productOrderQueryRequest.getSortField();
        String sortOrder = productOrderQueryRequest.getSortOrder();
        // 拼接查询条件
        if (StringUtils.isNotBlank(orderName)) {
            queryWrapper.like("orderName", orderName);
        }
        queryWrapper.eq(StringUtils.isNotBlank(orderNo), "orderNo", orderNo);
        queryWrapper.eq(StringUtils.isNotBlank(status), "status", status);
        queryWrapper.eq(StringUtils.isNotBlank(payType), "payType", payType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(productId), "productId", productId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDeleted", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public ProductOrderVO saveProductOrder(Long productId, UserVO loginUser) {
        ProductInfo productInfo = productInfoService.getById(productId);
        if (productInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        // 5分钟有效期
        Date date = DateUtil.date(System.currentTimeMillis());
        Date expirationTime = DateUtil.offset(date, DateField.MINUTE, 5);
        String orderNo = ORDER_PREFIX + RandomUtil.randomNumbers(20);

        ProductOrder productOrder = new ProductOrder();
        productOrder.setUserId(loginUser.getId());
        productOrder.setOrderNo(orderNo);
        productOrder.setProductId(productInfo.getId());
        productOrder.setOrderName(productInfo.getProductName());
        productOrder.setTotal(productInfo.getPrice());
        productOrder.setStatus(ProductOrderStatusEnum.NOTPAY.getValue());
        productOrder.setPayType(ZFB.getValue());
        productOrder.setExpirationTime(expirationTime);
        productOrder.setProductInfo(JSONUtil.toJsonPrettyStr(productInfo));
        productOrder.setAddPoints(productInfo.getAddCoin());

        boolean saveResult = this.save(productOrder);

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);
        model.setSubject(productInfo.getProductName());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 金额四舍五入
        BigDecimal scaledAmount = new BigDecimal(productInfo.getPrice()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        model.setTotalAmount(String.valueOf(scaledAmount));
        model.setBody(productInfo.getDescription());

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(aliPayAccountConfig.getNotifyUrl());
        request.setReturnUrl(aliPayAccountConfig.getReturnUrl());

        try {
            AlipayTradePagePayResponse alipayTradePagePayResponse = AliPayApi.pageExecute(request);
            String payUrl = alipayTradePagePayResponse.getBody();
            productOrder.setFromData(payUrl);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }

        boolean updateResult = this.updateProductOrder(productOrder);
        if (!updateResult & !saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        // 构建vo
        ProductOrderVO productOrderVo = new ProductOrderVO();
        BeanUtils.copyProperties(productOrder, productOrderVo);
        productOrderVo.setProductInfo(productInfo);
        productOrderVo.setTotal(productInfo.getPrice().toString());
        productOrderVo.setFormData(productOrder.getFromData());
        return productOrderVo;
    }

    @Override
    public boolean updateProductOrder(ProductOrder productOrder) {
        String fromData = productOrder.getFromData();
        Long id = productOrder.getId();
        ProductOrder updateCodeUrl = new ProductOrder();
        updateCodeUrl.setFromData(fromData);
        updateCodeUrl.setId(id);
        return this.updateById(updateCodeUrl);
    }

    @Override
    public ProductOrderVO getProductOrder(Long productId, UserVO loginUser) {
        LambdaQueryWrapper<ProductOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrder::getProductId, productId);
        lambdaQueryWrapper.eq(ProductOrder::getStatus, NOTPAY.getValue());
        lambdaQueryWrapper.eq(ProductOrder::getUserId, loginUser);
        ProductOrder oldOrder = this.getOne(lambdaQueryWrapper);
        if (oldOrder == null) {
            return null;
        }
        ProductOrderVO productOrderVO = new ProductOrderVO();
        BeanUtils.copyProperties(oldOrder, productOrderVO);
        productOrderVO.setProductInfo(JSONUtil.toBean(oldOrder.getProductInfo(), ProductInfo.class));
        productOrderVO.setTotal(oldOrder.getTotal().toString());
        return productOrderVO;
    }

    @Override
    public boolean updateOrderStatusByOrderNo(String orderNo, String orderStatus) {
        LambdaQueryWrapper<ProductOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrder::getOrderNo, orderNo);
        ProductOrder productOrder = this.getOne(lambdaQueryWrapper);
        if (ObjectUtils.isEmpty(productOrder)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        ProductOrder order = new ProductOrder();
        if (ProductOrderStatusEnum.getEnumByValue(orderStatus) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        order.setStatus(orderStatus);
        order.setId(productOrder.getId());
        return updateById(order);
    }

    @Override
    public void closeOrderByOrderNo(String orderNo) throws AlipayApiException {
        AlipayTradeCloseModel alipayTradeCloseModel = new AlipayTradeCloseModel();
        alipayTradeCloseModel.setTradeNo(orderNo);
        AlipayTradeCloseRequest alipayTradeCloseRequest = new AlipayTradeCloseRequest();
        alipayTradeCloseRequest.setBizModel(alipayTradeCloseModel);
        AliPayApi.doExecute(alipayTradeCloseRequest);
    }

    @Override
    public ProductOrder getProductOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<ProductOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrder::getOrderNo, orderNo);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processingTimeOutOrders(ProductOrder productOrder) {
        String orderNo = productOrder.getOrderNo();

        try {
            // 查询订单
            AlipayTradeQueryModel alipayTradeQueryModel = new AlipayTradeQueryModel();
            alipayTradeQueryModel.setOutTradeNo(orderNo);
            AlipayTradeQueryResponse queryResponse = AliPayApi.tradeQueryToResponse(alipayTradeQueryModel);

            // 本地创建了订单,但是用户没有扫码,支付宝端没有订单
            if (!queryResponse.getCode().equals(RESPONSE_CODE_SUCCESS)) {
                this.updateOrderStatusByOrderNo(orderNo, ProductOrderStatusEnum.CLOSED.getValue());
                log.info("超时订单{},跟新成功", orderNo);
                return;
            }
            String tradeStatus = AlipayTradeStatusEnum.findByName(queryResponse.getTradeStatus()).getPaymentStatusEnum().getValue();
            // 订单没有支付就关闭订单,更新本地订单状态
            if (tradeStatus.equals(NOTPAY.getValue()) || tradeStatus.equals(CLOSED.getValue())) {
                closeOrderByOrderNo(orderNo);
                this.updateOrderStatusByOrderNo(orderNo, CLOSED.getValue());
                log.info("超时订单{},关闭成功", orderNo);
                return;
            }
            if (tradeStatus.equals(SUCCESS.getValue())) {
                // 订单已支付更新商户端订单状态
                boolean updateOrderStatus = this.updateOrderStatusByOrderNo(orderNo, SUCCESS.getValue());
                // 补发积分到用户钱包
                boolean addCoinBalance = userService.addCoinBalance(productOrder.getUserId(), new Long(productOrder.getAddPoints()));
                // 保存支付记录
                PaymentInfoVO paymentInfoVO = new PaymentInfoVO();
                paymentInfoVO.setAppid(aliPayAccountConfig.getAppId());
                paymentInfoVO.setOutTradeNo(queryResponse.getOutTradeNo());
                paymentInfoVO.setTransactionId(queryResponse.getTradeNo());
                paymentInfoVO.setTradeType("电脑支付");
                paymentInfoVO.setTradeState(queryResponse.getTradeStatus());
                paymentInfoVO.setTradeStateDesc("支付成功");
                paymentInfoVO.setSuccessTime(String.valueOf(queryResponse.getSendPayDate()));
                boolean servicePaymentInfo = paymentInfoService.createPaymentInfo(paymentInfoVO);
                if (!updateOrderStatus & !addCoinBalance & !servicePaymentInfo) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                // 更新充值记录表
                saveRechargeRecord(productOrder);
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String doPaymentNotify(String notifyData, HttpServletRequest request) {
        Map<String, String> params = AliPayApi.toMap(request);
        AliPayAsyncResponse aliPayAsyncResponse = JSONUtil.toBean(JSONUtil.toJsonStr(params), AliPayAsyncResponse.class);
        String lockName = "notify:AlipayOrder:lock:" + aliPayAsyncResponse.getOutTradeNo();
        return redissonLockUtil.redissonDistributedLocks(lockName, "【支付宝异步回调异常】:", () -> {
            String result;
            try {
                result = checkAlipayOrder(aliPayAsyncResponse, params);
            } catch (AlipayApiException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
            if (!"success".equals(result)) {
                return result;
            }
            String doAliPayOrderBusiness = this.doAliPayOrderBusiness(aliPayAsyncResponse);
            if (StringUtils.isBlank(doAliPayOrderBusiness)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            return doAliPayOrderBusiness;
        });
    }

    private String checkAlipayOrder(AliPayAsyncResponse response, Map<String, String> params) throws AlipayApiException {
        String result = "failure";
        boolean verifyResult = AlipaySignature.rsaCheckV1(params, AliPayApiConfigKit.getAliPayApiConfig().getAliPayPublicKey(),
                AliPayApiConfigKit.getAliPayApiConfig().getCharset(),
                AliPayApiConfigKit.getAliPayApiConfig().getSignType());
        if (!verifyResult) {
            return result;
        }
        // 1.验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号。
        ProductOrder productOrder = this.getProductOrderByOrderNo(response.getOutTradeNo());
        if (productOrder == null) {
            log.error("订单不存在");
            return result;
        }
        // 2.判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）。
        int totalAmount = new BigDecimal(response.getTotalAmount()).multiply(new BigDecimal("100")).intValue();
        if (totalAmount != productOrder.getTotal()) {
            log.error("订单金额不一致");
            return result;
        }
        // 3.校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方（有的时候，一个商家可能有多个 seller_id/seller_email）。
        String sellerId = aliPayAccountConfig.getSellerId();
        if (!response.getSellerId().equals(sellerId)) {
            log.error("卖家账号校验失败");
            return result;
        }
        // 4.验证 app_id 是否为该商家本身。
        String appId = aliPayAccountConfig.getAppId();
        if (!response.getAppId().equals(appId)) {
            log.error("appid校验失败");
            return result;
        }
        // 状态 TRADE_SUCCESS 的通知触发条件是商家开通的产品支持退款功能的前提下，买家付款成功。
        String tradeStatus = response.getTradeStatus();
        if (!tradeStatus.equals(TRADE_SUCCESS)) {
            log.error("交易失败");
            return result;
        }
        return "success";
    }

    @SneakyThrows
    protected String doAliPayOrderBusiness(AliPayAsyncResponse response) {
        String outTradeNo = response.getOutTradeNo();
        ProductOrder productOrder = this.getProductOrderByOrderNo(outTradeNo);
        // 处理重复通知
        if (SUCCESS.getValue().equals(productOrder.getStatus())) {
            return "success";
        }
        // 更新订单状态
        boolean updateOrderStatus = this.updateOrderStatusByOrderNo(outTradeNo, SUCCESS.getValue());
        boolean addCoinBalance = userService.addCoinBalance(productOrder.getUserId(), new Long(productOrder.getAddPoints()));
        // 保存支付记录
        PaymentInfoVO paymentInfoVo = new PaymentInfoVO();
        paymentInfoVo.setAppid(response.getAppId());
        paymentInfoVo.setOutTradeNo(response.getOutTradeNo());
        paymentInfoVo.setTransactionId(response.getTradeNo());
        paymentInfoVo.setTradeType("电脑网站支付");
        paymentInfoVo.setTradeState(response.getTradeStatus());
        paymentInfoVo.setTradeStateDesc("支付成功");
        paymentInfoVo.setSuccessTime(response.getNotifyTime());
        boolean paymentResult = paymentInfoService.createPaymentInfo(paymentInfoVo);
        // 更新活动表
        boolean rechargeActivity = saveRechargeRecord(productOrder);
        if (paymentResult && updateOrderStatus && addCoinBalance && rechargeActivity) {
            log.info("【支付回调通知处理成功】");
            // 发送邮件
            sendSuccessEmail(productOrder, response.getTotalAmount());
            return "success";
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR);
    }


    /**
     * 发送支付成功邮件
     *
     * @param productOrder
     * @param orderTotal
     */
    private void sendSuccessEmail(ProductOrder productOrder, String orderTotal) {
        User user = userService.getById(productOrder.getUserId());
        if (StringUtils.isNotBlank(user.getEmail())) {
            try {
                ProductOrder productOrderByOrderNo = this.getProductOrderByOrderNo(productOrder.getOrderNo());
                new EmailUtil().sendPaySuccessEmail(user.getEmail(), mailSender, emailConfig, productOrderByOrderNo.getOrderName(), productOrderByOrderNo.getTotal().toString());
                log.info("发送邮件:{} , 成功", user.getEmail());
            } catch (MessagingException e) {
                log.info("发送邮件:{} , 失败:{}", user.getEmail(), e);
            }
        }
    }

    /**
     * 保存充值记录
     *
     * @param productOrder
     * @return
     */
    private boolean saveRechargeRecord(ProductOrder productOrder) {
        RechargeRecord rechargeRecord = new RechargeRecord();
        Long userId = productOrder.getUserId();
        Long productId = productOrder.getProductId();
        String orderNo = productOrder.getOrderNo();
        rechargeRecord.setUserId(userId);
        rechargeRecord.setOrderNo(orderNo);
        rechargeRecord.setProductId(productId);
        boolean save = rechargeRecordService.save(rechargeRecord);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }
}
