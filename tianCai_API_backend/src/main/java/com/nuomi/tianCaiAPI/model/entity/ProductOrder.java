package com.nuomi.tianCaiAPI.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品订单表
 * @TableName product_order
 */
@TableName(value ="product_order")
@Data
public class ProductOrder implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户id
     */
    private Long productId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商品名
     */
    private String orderName;

    /**
     * 总金额
     */
    private Long total;

    /**
     * 交易状态(SUCCESS：支付成功 REFUND：转入退款 NOTPAY：未支付 CLOSED：已关闭 REVOKED：
                                                                            已撤销（仅付款码支付会返回) USERPAYING：用户支付中（仅付款码支付会返回）PAYERROR：支付失败（仅付款码支付会返回）)
     */
    private String status;

    /**
     * 支付方式(ZFB 支付宝)
     */
    private String payType;

    /**
     * 商品信息
     */
    private String productInfo;

    /**
     * 支付宝formData
     */
    private String fromData;

    /**
     * 增加甜菜币数
     */
    private Integer addPoints;

    /**
     * 过期时间
     */
    private Date expirationTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}