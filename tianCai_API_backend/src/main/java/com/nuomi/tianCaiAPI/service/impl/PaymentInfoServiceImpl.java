package com.nuomi.tianCaiAPI.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuomi.tianCaiAPI.model.entity.PaymentInfo;
import com.nuomi.tianCaiAPI.model.vo.PaymentInfoVO;
import com.nuomi.tianCaiAPI.service.PaymentInfoService;
import com.nuomi.tianCaiAPI.mapper.PaymentInfoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
    implements PaymentInfoService{

    @Override
    public boolean createPaymentInfo(PaymentInfoVO paymentInfoVO) {
        String transactionId = paymentInfoVO.getTransactionId();
        String tradeType = paymentInfoVO.getTradeType();
        String tradeState = paymentInfoVO.getTradeState();
        String tradeStateDesc = paymentInfoVO.getTradeStateDesc();
        String successTime = paymentInfoVO.getSuccessTime();
        String outTradeNo = paymentInfoVO.getOutTradeNo();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(outTradeNo);
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setTradeStateDesc(tradeStateDesc);
        paymentInfo.setSuccessTime(successTime);
        boolean saveResult = this.save(paymentInfo);
        return saveResult;
    }
}




