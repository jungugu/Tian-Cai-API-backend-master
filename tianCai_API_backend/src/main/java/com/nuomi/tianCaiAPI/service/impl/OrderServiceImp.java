package com.nuomi.tianCaiAPI.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.model.entity.ProductInfo;
import com.nuomi.tianCaiAPI.model.entity.ProductOrder;
import com.nuomi.tianCaiAPI.model.entity.RechargeRecord;
import com.nuomi.tianCaiAPI.model.enums.ProductOrderStatusEnum;
import com.nuomi.tianCaiAPI.model.enums.ProductTypeStatusEnum;
import com.nuomi.tianCaiAPI.model.vo.ProductOrderVO;
import com.nuomi.tianCaiAPI.model.vo.UserVO;
import com.nuomi.tianCaiAPI.service.OrderService;
import com.nuomi.tianCaiAPI.service.ProductInfoService;
import com.nuomi.tianCaiAPI.service.ProductOrderService;
import com.nuomi.tianCaiAPI.service.RechargeRecordService;
import com.nuomi.tianCaiAPI.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.nuomi.tianCaiAPI.model.enums.OrderPayTypeEnum.ZFB;

/**
 * 订单服务
 * @author NuoMi
 */
@Slf4j
@Service
public class OrderServiceImp implements OrderService {

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private RechargeRecordService rechargeRecordService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private RedissonLockUtil redissonLockUtil;
    @Override
    public String doOrderNotify(String notifyDate, HttpServletRequest request) {
        return productOrderService.doPaymentNotify(notifyDate, request);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrderVO createOrder(Long productId, UserVO loginUser) {
        String lock = ("getOrder_" + loginUser.getUserAccount()).intern();
        ProductOrderVO productOrderVO = redissonLockUtil.redissonDistributedLocks(lock, () -> {
            // 订单存在就返回不再新创建
            return productOrderService.getProductOrder(productId, loginUser);
        });
        if (productOrderVO != null) {
            return productOrderVO;
        }
        lock = ("createOrder_" + loginUser.getUserAccount()).intern();
        // 分布式锁工具
        return redissonLockUtil.redissonDistributedLocks(lock, () -> {
            // 检查是否购买充值活动
            checkBuyRechargeActivity(loginUser.getId(), productId);
            // 保存订单,返回vo信息
            return productOrderService.saveProductOrder(productId, loginUser);
        });
    }

    /**
     * 检查购买充值记录
     *
     * @param userId    用户id
     * @param productId 产品订单id
     */
    private void checkBuyRechargeActivity(Long userId, Long productId) {
        ProductInfo productInfo = productInfoService.getById(productId);
        if (productInfo.getProductType().equals(ProductTypeStatusEnum.RECHARGE_ACTIVITY.getValue())) {
            LambdaQueryWrapper<ProductOrder> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderLambdaQueryWrapper.eq(ProductOrder::getUserId, userId);
            orderLambdaQueryWrapper.eq(ProductOrder::getProductId, productId);
            orderLambdaQueryWrapper.eq(ProductOrder::getStatus, ProductOrderStatusEnum.NOTPAY.getValue());
            orderLambdaQueryWrapper.or().eq(ProductOrder::getStatus, ProductOrderStatusEnum.SUCCESS.getValue());

            long orderCount = productOrderService.count(orderLambdaQueryWrapper);
            if (orderCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该商品只能购买一次，请查看是否已经创建了该订单，或者挑选其他商品吧！");
            }
            LambdaQueryWrapper<RechargeRecord> activityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            activityLambdaQueryWrapper.eq(RechargeRecord::getUserId, userId);
            activityLambdaQueryWrapper.eq(RechargeRecord::getProductId, productId);
            long count = rechargeRecordService.count(activityLambdaQueryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该商品只能购买一次，请查看是否已经创建了该订单，或者挑选其他商品吧！！");
            }
        }
    }

    /**
     * 获取超时的订单
     * @param minutes
     * @param remove
     * @param payType
     * @return
     */
    @Override
    public List<ProductOrder> getNoPayOrderByDuration(int minutes, Boolean remove, String payType) {
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));
        LambdaQueryWrapper<ProductOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrder::getStatus, ProductOrderStatusEnum.NOTPAY.getValue());
        if (remove) {
            lambdaQueryWrapper.or().eq(ProductOrder::getStatus, ProductOrderStatusEnum.CLOSED.getValue());
        }
        lambdaQueryWrapper.and(p ->p.le(ProductOrder::getCreateTime, instant));
        return productOrderService.list(lambdaQueryWrapper);
    }
}
