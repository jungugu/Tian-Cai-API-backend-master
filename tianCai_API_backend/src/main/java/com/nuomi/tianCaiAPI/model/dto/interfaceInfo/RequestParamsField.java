package com.nuomi.tianCaiAPI.model.dto.interfaceInfo;

import lombok.Data;

/**
 * @author NuoMi
 */
@Data
public class RequestParamsField {
    private String id;
    private String fieldName;
    private String type;
    private String desc;
    private String required;
}
