package com.nuomi.tianCaiAPI.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户于调用接口表
 * @author NuoMi
 * @TableName product_info
 */
@TableName(value ="product_info")
@Data
public class ProductInfo implements Serializable {
    /**
     * 商品id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品名
     */
    private String productName;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 价格
     */
    private Long price;

    /**
     * 状态(0下线1上线)
     */
    private Integer status;

    /**
     * 增加甜菜币数
     */
    private Integer addCoin;

    /**
     * 产品类型（VIP-会员 RECHARGE-充值,RECHARGEACTIVITY-充值活动）
     */
    private String productType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableLogic
    /**
     * 是否删除(0-未删, 1-已删)
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}