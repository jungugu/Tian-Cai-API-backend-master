package com.nuomi.tianCaiAPI.model.dto.interfaceInfo;

import com.nuomi.tianCaiAPI.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author NuoMi
 */
@Data
public class InterfaceSearchRequest extends PageRequest implements Serializable {
    private String searchText;
    private static final long serialVersionUID = 1L;
}
