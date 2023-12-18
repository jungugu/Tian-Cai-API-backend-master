package com.nuomi.tianCaiAPI.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单状态
 * @author NuoMi
 */
public enum ProductOrderStatusEnum {
    /**
     *
     */
    SUCCESS("支付成功","SUCCESS"),
    NOTPAY("未支付", "NOTPAY"),
    REFUND("转入退款", "REFUND"),
    CLOSED("已关闭", "CLOSED"),
    REVOKED("已撤销", "REVOKED"),
    USERPAYING("用户支付中", "USERPAYING"),
    PAYERROR("支付失败", "PAYERROR"),
    ;

    private final String text;

    private final String value;

    ProductOrderStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ProductOrderStatusEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ProductOrderStatusEnum anEnum : ProductOrderStatusEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
