package com.nuomi.tianCaiAPI.model.enums;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品状态
 * @author NuoMi
 */
public enum ProductInfoStatusEnum {
    /**
     *
     */
    DEFAULT("审核中",0),
    ONLINE("已上线", 1),
    OFFLINE("已下线", 2)
    ;

    private final String text;

    private final Integer value;

    ProductInfoStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ProductInfoStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ProductInfoStatusEnum anEnum : ProductInfoStatusEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public Integer getValue() {
        return value;
    }
}
