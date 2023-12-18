package com.nuomi.tianCaiAPI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nuomi.tianCaiAPI.model.dto.post.PostQueryRequest;
import com.nuomi.tianCaiAPI.model.dto.pruductOrder.ProductOrderQueryRequest;
import com.nuomi.tianCaiAPI.model.entity.Post;
import com.nuomi.tianCaiAPI.model.entity.ProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nuomi.tianCaiAPI.model.vo.ProductOrderVO;
import com.nuomi.tianCaiAPI.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author NuoMi
 */
public interface ProductOrderService extends IService<ProductOrder> {

    /**
     * 获取查询条件
     *
     * @param productOrderQueryRequest
     * @return
     */
    QueryWrapper<ProductOrder> getQueryWrapper(ProductOrderQueryRequest productOrderQueryRequest);

    /**
     * 保存产品订单
     * @param productId
     * @param loginUser
     * @return
     */
    ProductOrderVO saveProductOrder(Long productId, UserVO loginUser);

    /**
     * 更新产品订单
     * @param productOrder
     * @return
     */
    boolean updateProductOrder(ProductOrder productOrder);

    /**
     * 获取订单
     * @param productId
     * @param loginUser
     * @return
     */
    ProductOrderVO getProductOrder(Long productId, UserVO loginUser);

    /**
     * 按订单号更新订单状态
     * @param orderNo
     * @param orderStatus
     * @return
     */
    boolean updateOrderStatusByOrderNo(String orderNo, String orderStatus);

    /**
     * 按订单号关闭订单
      * @param orderNo
     * @throws Exception
     */
    void closeOrderByOrderNo(String orderNo) throws Exception;

    /**
     * 通过orderNo获取productOrder
     * @param orderNo
     * @return
     */
    ProductOrder getProductOrderByOrderNo(String orderNo);

    /**
     * 处理超时的订单
     * @param productOrder
     */
    void processingTimeOutOrders(ProductOrder productOrder);

    /**
     * 付款通知
     * @param notifyData
     * @param request
     * @return
     */
    String doPaymentNotify(String notifyData, HttpServletRequest request);
}
