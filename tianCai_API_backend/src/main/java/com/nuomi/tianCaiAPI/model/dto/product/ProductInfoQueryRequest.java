package com.nuomi.tianCaiAPI.model.dto.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.nuomi.tianCaiAPI.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProductInfoQueryRequest extends PageRequest implements Serializable {
    /**
     * 商品id
     */
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
     * 是否删除(0-未删, 1-已删)
     */
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}