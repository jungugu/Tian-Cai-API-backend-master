package com.nuomi.tianCaiAPI.model.vo;

import com.google.gson.Gson;
import com.tiancaiapi.common.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InterfaceInfoVO extends InterfaceInfo implements Serializable {
    private Long totalInvokes;
    private static final long serialVersionUID = 1L;
}
