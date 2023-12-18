package com.nuomi.tianCaiAPI.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author NuoMi
 */
@Data
public class AnalysisOrderVO implements Serializable {
    private String date;

    private Long total;
}
