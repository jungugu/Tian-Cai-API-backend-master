package com.nuomi.tianCaiAPI.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author NuoMi
 */
public enum UserInterfaceInfoStatusEnum {
    OFFLINE("正常",0),
    ONLINE("禁用", 1)
    ;

    private final String text;

    private final Integer value;

    UserInterfaceInfoStatusEnum(String text, Integer value) {
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
    public static UserInterfaceInfoStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (UserInterfaceInfoStatusEnum anEnum : UserInterfaceInfoStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
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
