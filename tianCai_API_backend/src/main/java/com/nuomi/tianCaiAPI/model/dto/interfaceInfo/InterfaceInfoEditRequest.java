package com.nuomi.tianCaiAPI.model.dto.interfaceInfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 *

 */
@Data
public class InterfaceInfoEditRequest implements Serializable {

    /**
     * 接口id
     */
    private Long id;

    /**
     * 接口名
     */
    private String name;

    /**
     * 接口头像
     */
    private String interfaceAvatar;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 返回格式（如json）
     */
    private String returnFormat;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应参数
     */
    private String responseParams;

    /**
     * 请求参数示例
     */
    private String requestExample;

    /**
     * 扣除积分数
     */
    private Integer reduceScore;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 接口状态(0关闭1开启)
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}