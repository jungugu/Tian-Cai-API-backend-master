package com.nuomi.tianCaiAPI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nuomi.tianCaiAPI.model.dto.product.ProductInfoQueryRequest;
import com.nuomi.tianCaiAPI.model.entity.Post;
import com.nuomi.tianCaiAPI.model.entity.ProductInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface ProductInfoService extends IService<ProductInfo> {
    /**
     * 校验
     *
     * @param productInfo
     * @param add
     */
    void validProductInfo(ProductInfo productInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param productInfoQueryRequest
     * @return
     */
    QueryWrapper<ProductInfo> getQueryWrapper(ProductInfoQueryRequest productInfoQueryRequest);
}
