package com.nuomi.tianCaiAPI.model.dto.pruductOrder;

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
 * @author NuoMi
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProductOrderQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品id
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
     * 交易状态(SUCCESS：支付成功 REFUND：转入退款 NOTPAY：未支付 CLOSED：已关闭 REVOKED：
     已撤销（仅付款码支付会返回) USERPAYING：用户支付中（仅付款码支付会返回）PAYERROR：支付失败（仅付款码支付会返回）)
     */
    private String status;

    /**
     * 支付方式(ZFB 支付宝)
     */
    private String payType;

    /**
     * 搜索
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}