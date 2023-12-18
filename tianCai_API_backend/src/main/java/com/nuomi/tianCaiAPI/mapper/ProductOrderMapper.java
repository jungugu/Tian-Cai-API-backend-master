package com.nuomi.tianCaiAPI.mapper;

import com.nuomi.tianCaiAPI.model.entity.ProductOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Entity com.nuomi.tianCaiAPI.model.entity.ProductOrder
 */
public interface ProductOrderMapper extends BaseMapper<ProductOrder> {

    /**
     * 获取本周的订单
     * @return
     */
    List<ProductOrder> listOrderInWeek();
}




