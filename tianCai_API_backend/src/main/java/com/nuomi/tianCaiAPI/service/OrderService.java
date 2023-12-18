package com.nuomi.tianCaiAPI.service;

import com.nuomi.tianCaiAPI.model.entity.ProductOrder;
import com.nuomi.tianCaiAPI.model.vo.ProductOrderVO;
import com.nuomi.tianCaiAPI.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author NuoMi
 */
public interface OrderService {
    /**
     * 处理订单通知
     * @param notifyDate
     * @param request
     * @return
     */
    String doOrderNotify(String notifyDate, HttpServletRequest request);

    /**
     * 创建订单
     * @param productId
     * @param loginUser
     * @return
     */
    ProductOrderVO createOrder(Long productId, UserVO loginUser);

    /**
     * 按时间获得未支付订单
     * @param minutes
     * @param remove
     * @param payType
     * @return
     */
    List<ProductOrder> getNoPayOrderByDuration(int minutes, Boolean remove, String payType);
}


