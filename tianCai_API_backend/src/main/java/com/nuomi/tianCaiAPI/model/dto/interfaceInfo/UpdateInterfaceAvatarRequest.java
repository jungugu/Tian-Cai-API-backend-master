package com.nuomi.tianCaiAPI.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author NuoMi
 */
@Data
public class UpdateInterfaceAvatarRequest implements Serializable {
    private Long id;
    private String interfaceAvatar;
    private static final long serialVersionUID = 1L;
}
