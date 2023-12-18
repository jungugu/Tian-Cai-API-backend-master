package com.nuomi.tianCaiAPI.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author NuoMi
 */
@Data
public class PaymentInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String appid;

    private String mchid;

    private String outTradeNo;

    private String transactionId;

    /**
     * 贸易类型
     */
    private String tradeType;

    private String tradeState;

    private String tradeStateDesc;

    private String bankType;

    private String attach;

    private String successTime;
}
