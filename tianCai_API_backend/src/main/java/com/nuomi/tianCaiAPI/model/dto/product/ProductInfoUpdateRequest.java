package com.nuomi.tianCaiAPI.model.dto.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 更新请求
 *

 */
@Data
public class ProductInfoUpdateRequest implements Serializable {

    /**
     * 商品id
     */
    private Long id;

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

    private static final long serialVersionUID = 1L;
}