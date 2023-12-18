package com.nuomi.tianCaiAPI.model.dto.pruductOrder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建请求
 *
 * @author NuoMi
 */
@Data
public class ProductOrderAddRequest implements Serializable {

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付方式(ZFB 支付宝)
     */
    private String payType;

    private static final long serialVersionUID = 1L;
}