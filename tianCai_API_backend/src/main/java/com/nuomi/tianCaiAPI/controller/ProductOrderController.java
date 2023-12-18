package com.nuomi.tianCaiAPI.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.nuomi.tianCaiAPI.annotation.AuthCheck;
import com.nuomi.tianCaiAPI.common.BaseResponse;
import com.nuomi.tianCaiAPI.common.DeleteRequest;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.common.ResultUtils;
import com.nuomi.tianCaiAPI.constant.UserConstant;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.exception.ThrowUtils;
import com.nuomi.tianCaiAPI.model.dto.pruductOrder.ProductOrderAddRequest;
import com.nuomi.tianCaiAPI.model.dto.pruductOrder.ProductOrderQueryRequest;
import com.nuomi.tianCaiAPI.model.entity.ProductInfo;
import com.nuomi.tianCaiAPI.model.entity.ProductOrder;
import com.nuomi.tianCaiAPI.model.enums.OrderPayTypeEnum;
import com.nuomi.tianCaiAPI.model.enums.ProductOrderStatusEnum;
import com.nuomi.tianCaiAPI.model.vo.OrderVo;
import com.nuomi.tianCaiAPI.model.vo.ProductOrderVO;
import com.nuomi.tianCaiAPI.model.vo.UserVO;
import com.nuomi.tianCaiAPI.service.OrderService;
import com.nuomi.tianCaiAPI.service.ProductOrderService;
import com.nuomi.tianCaiAPI.service.UserService;
import com.tiancaiapi.common.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.nuomi.tianCaiAPI.constant.PayConstant.QUERY_ORDER_STATUS;

/**
 * 订单接口
 *
 * @author NuoMi
 */
@RestController
@RequestMapping("/productOrder")
@Slf4j
public class ProductOrderController {

    @Resource
    private ProductOrderService productOrderService;

    @Resource
    private OrderService orderService;

    @Resource
    private UserService userService;

    private RedisTemplate<String, Boolean> redisTemplate;

    private final static Gson GSON = new Gson();

    // region 增删改查

    @PostMapping("/close")
    public BaseResponse<Boolean> closeProductOrder(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductOrder productOrder = productOrderService.getProductOrderByOrderNo(orderNo);
        if (ObjectUtils.isEmpty(productOrder)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean closeResult = productOrderService.updateOrderStatusByOrderNo(orderNo, ProductOrderStatusEnum.CLOSED.getValue());
        return ResultUtils.success(closeResult);
    }

    /**
     * 创建
     *
     * @param productOrderAddRequest
     * @param request
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<ProductOrderVO> addProductOrder(@RequestBody ProductOrderAddRequest productOrderAddRequest, HttpServletRequest request) {
        if (productOrderAddRequest == null && productOrderAddRequest.getProductId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long productId = productOrderAddRequest.getProductId();
        String payType = productOrderAddRequest.getPayType();
        if (OrderPayTypeEnum.getEnumByValue(payType) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "不支持该支付方式");
        }
        User loginUser = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(loginUser, userVO);
        ProductOrderVO productOrderVO = orderService.createOrder(productId,userVO);
        return ResultUtils.success(productOrderVO);
    }

    /**
     * 删除
     *
     * @param id 订单id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteProductOrder(int id, HttpServletRequest request) {
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        // 判断是否存在
        ProductOrder oldProductOrder = productOrderService.getById(id);
        ThrowUtils.throwIf(oldProductOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldProductOrder.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = productOrderService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<ProductOrderVO> getProductOrderById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductOrder productOrder = productOrderService.getById(id);
        if (productOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        ProductOrderVO productOrderVo = formatProductOrderVo(productOrder);
        return ResultUtils.success(productOrderVo);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param productOrderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ProductOrder>> listProductOrderByPage(@RequestBody ProductOrderQueryRequest productOrderQueryRequest,
            HttpServletRequest request) {
        long current = productOrderQueryRequest.getCurrent();
        long size = productOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ProductOrder> productOrderPage = productOrderService.page(new Page<>(current, size),
                productOrderService.getQueryWrapper(productOrderQueryRequest));
        return ResultUtils.success(productOrderPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param productOrderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<OrderVo> listMyProductOrderByPage(@RequestBody ProductOrderQueryRequest productOrderQueryRequest,
            HttpServletRequest request) {
        if (productOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        productOrderQueryRequest.setUserId(loginUser.getId());
        long current = productOrderQueryRequest.getCurrent();
        long size = productOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ProductOrder> productOrderPage = productOrderService.page(new Page<>(current, size),
                productOrderService.getQueryWrapper(productOrderQueryRequest).last("ORDER BY CASE WHEN status = 'NOTPAY' THEN 0 ELSE 1 END, status"));
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(productOrderPage, orderVo);
        // 处理订单信息,
        List<ProductOrderVO> productOrders = productOrderPage.getRecords().stream().map(this::formatProductOrderVo).collect(Collectors.toList());
        orderVo.setRecords(productOrders);
        return ResultUtils.success(orderVo);
    }
    // endregion

    /**
     * 查询订单状态
     * @param productOrderQueryRequest
     * @return
     */
    @PostMapping("/query/status")
    public BaseResponse<Boolean> queryOrderStatus(@RequestBody ProductOrderQueryRequest productOrderQueryRequest) {
        if (ObjectUtils.isEmpty(productOrderQueryRequest) || StringUtils.isBlank(productOrderQueryRequest.getOrderNo())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String orderNo = productOrderQueryRequest.getOrderNo();
        Boolean data = redisTemplate.opsForValue().get(QUERY_ORDER_STATUS + orderNo);
        if (Boolean.FALSE.equals(data)) {
            return ResultUtils.success(data);
        }
        ProductOrder productOrder = productOrderService.getProductOrderByOrderNo(orderNo);
        if (ProductOrderStatusEnum.SUCCESS.getValue().equals(productOrder.getStatus())) {
            return ResultUtils.success(true);
        }
        redisTemplate.opsForValue().set(QUERY_ORDER_STATUS + orderNo, false, 5, TimeUnit.MINUTES);
        return ResultUtils.success(false);
    }

    /**
     *  解析订单通知结果
     *  通知频率为15s/15s/30s/3m/10m/20m/30m/30m/30m/60m/3h/3h/3h/6h/6h - 总计 24h4m
     * @return
     */
    @PostMapping("/notify/order")
    public String parseOrderNotifyResult(@RequestBody String notifyData, HttpServletRequest request) {
        return productOrderService.doPaymentNotify(notifyData, request);
    }

    private ProductOrderVO formatProductOrderVo(ProductOrder productOrder) {
        ProductOrderVO productOrderVo = new ProductOrderVO();
        BeanUtils.copyProperties(productOrder, productOrderVo);
        ProductInfo prodInfo = JSONUtil.toBean(productOrder.getProductInfo(), ProductInfo.class);
        productOrderVo.setDescription(prodInfo.getDescription());
        productOrderVo.setProductType(prodInfo.getProductType());
        String voTotal = String.valueOf(prodInfo.getPrice());
        BigDecimal total = new BigDecimal(voTotal).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        productOrderVo.setTotal(total.toString());
        return productOrderVo;
    }
}
