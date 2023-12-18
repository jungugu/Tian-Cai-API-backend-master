package com.nuomi.tianCaiAPI.model.vo;

import com.tiancaiapi.common.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author NuoMi
 */
@Data
public class AnalysisUserRegisterVO implements Serializable {
    private String date;

    private Integer registerUserNum;
}
