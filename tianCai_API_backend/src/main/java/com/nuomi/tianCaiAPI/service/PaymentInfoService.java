package com.nuomi.tianCaiAPI.service;

import com.nuomi.tianCaiAPI.model.entity.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nuomi.tianCaiAPI.model.vo.PaymentInfoVO;

/**
 *
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    /**
     * 创建付款信息
     * @param paymentInfoVO
     * @return
     */
    boolean createPaymentInfo(PaymentInfoVO paymentInfoVO);
}
