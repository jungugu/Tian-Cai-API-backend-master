package com.tiancaiapi.common.entity.dto.userInterfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;

    /**
     * 状态(0正常1禁用)
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}