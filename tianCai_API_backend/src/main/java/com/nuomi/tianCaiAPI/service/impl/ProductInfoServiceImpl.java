package com.nuomi.tianCaiAPI.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.constant.CommonConstant;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.exception.ThrowUtils;
import com.nuomi.tianCaiAPI.model.dto.product.ProductInfoQueryRequest;
import com.nuomi.tianCaiAPI.model.entity.Post;
import com.nuomi.tianCaiAPI.model.entity.ProductInfo;
import com.nuomi.tianCaiAPI.model.enums.ProductInfoStatusEnum;
import com.nuomi.tianCaiAPI.service.ProductInfoService;
import com.nuomi.tianCaiAPI.mapper.ProductInfoMapper;
import com.nuomi.tianCaiAPI.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo>
    implements ProductInfoService{
    @Override
    public void validProductInfo(ProductInfo productInfo, boolean add) {
        if (productInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String productName = productInfo.getProductName();
        String description = productInfo.getDescription();
        Long price = productInfo.getPrice();
        Integer status = productInfo.getStatus();
        Integer addCoin = productInfo.getAddCoin();
        // 有参数则校验
        if (StringUtils.isEmpty(productName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称不能为空");
        }
        if (description.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
        if (0 > price) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格不合法");
        }
        if (addCoin < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "增加积分不合法");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param productInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ProductInfo> getQueryWrapper(ProductInfoQueryRequest productInfoQueryRequest) {
        QueryWrapper<ProductInfo> queryWrapper = new QueryWrapper<>();
        if (productInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = productInfoQueryRequest.getId();
        Long userId = productInfoQueryRequest.getUserId();
        String productName = productInfoQueryRequest.getProductName();
        String description = productInfoQueryRequest.getDescription();
        Integer status = productInfoQueryRequest.getStatus();
        Integer addCoin = productInfoQueryRequest.getAddCoin();
        String sortField = productInfoQueryRequest.getSortField();
        String sortOrder = productInfoQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(productName), "productName", productName);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.ne(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(addCoin), "addCoin", addCoin);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq("isDeleted", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




