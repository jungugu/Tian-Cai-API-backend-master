package com.nuomi.tianCaiAPI.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *

 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 请求参数
     */
    private List<Field> requestParams;

    private String userRequestParams;

    @Data
    public static  class Field {
        private String fieldName;
        private String value;
    }
    private static final long serialVersionUID = 1L;
}