package com.nuomi.tianCaiAPI.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.nuomi.tianCaiAPI.annotation.AuthCheck;
import com.nuomi.tianCaiAPI.common.BaseResponse;
import com.nuomi.tianCaiAPI.common.DeleteRequest;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.common.ResultUtils;
import com.nuomi.tianCaiAPI.constant.UserConstant;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.exception.ThrowUtils;
import com.nuomi.tianCaiAPI.model.dto.product.ProductInfoAddRequest;
import com.nuomi.tianCaiAPI.model.dto.product.ProductInfoQueryRequest;
import com.nuomi.tianCaiAPI.model.dto.product.ProductInfoUpdateRequest;
import com.nuomi.tianCaiAPI.model.dto.product.ProductOnlineRequest;
import com.nuomi.tianCaiAPI.model.entity.ProductInfo;
import com.nuomi.tianCaiAPI.model.enums.ProductInfoStatusEnum;
import com.nuomi.tianCaiAPI.service.ProductInfoService;
import com.nuomi.tianCaiAPI.service.UserService;
import com.tiancaiapi.common.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 商品信息接口
 *
 * @author NuoMi
 */
@RestController
@RequestMapping("/productInfo")
@Slf4j
public class ProductInfoController {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建(仅管理员)
     *
     * @param productInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addProductInfo(@RequestBody ProductInfoAddRequest productInfoAddRequest, HttpServletRequest request) {
        if (productInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductInfo productInfo = new ProductInfo();
        BeanUtils.copyProperties(productInfoAddRequest, productInfo);
        productInfoService.validProductInfo(productInfo, true);
        User loginUser = userService.getLoginUser(request);
        productInfo.setUserId(loginUser.getId());
        boolean result = productInfoService.save(productInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newProductInfoId = productInfo.getId();
        return ResultUtils.success(newProductInfoId);
    }

    /**
     * 删除(仅管理员)
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteProductInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ProductInfo oldProductInfo = productInfoService.getById(id);
        ThrowUtils.throwIf(oldProductInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldProductInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = productInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param productInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateProductInfo(@RequestBody ProductInfoUpdateRequest productInfoUpdateRequest) {
        if (productInfoUpdateRequest == null || productInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductInfo productInfo = new ProductInfo();
        BeanUtils.copyProperties(productInfoUpdateRequest, productInfo);
        // 参数校验
        productInfoService.validProductInfo(productInfo, false);
        long id = productInfoUpdateRequest.getId();
        // 判断是否存在
        ProductInfo oldProductInfo = productInfoService.getById(id);
        ThrowUtils.throwIf(oldProductInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = productInfoService.updateById(productInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<ProductInfo> getProductInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductInfo productInfo = productInfoService.getById(id);
        if (productInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(productInfo);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param productInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<ProductInfo>> listProductInfoByPage(@RequestBody ProductInfoQueryRequest productInfoQueryRequest,
            HttpServletRequest request) {
        long current = productInfoQueryRequest.getCurrent();
        long size = productInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ProductInfo> productInfoPage = productInfoService.page(new Page<>(current, size),
                productInfoService.getQueryWrapper(productInfoQueryRequest));
        return ResultUtils.success(productInfoPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param productInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<ProductInfo>> listMyProductInfoByPage(@RequestBody ProductInfoQueryRequest productInfoQueryRequest,
            HttpServletRequest request) {
        if (productInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        productInfoQueryRequest.setUserId(loginUser.getId());
        long current = productInfoQueryRequest.getCurrent();
        long size = productInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ProductInfo> productInfoPage = productInfoService.page(new Page<>(current, size),
                productInfoService.getQueryWrapper(productInfoQueryRequest));
        return ResultUtils.success(productInfoPage);
    }

    // endregion

    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineHandle(@RequestBody ProductOnlineRequest productOnlineRequest, HttpServletRequest request) {
        if (productOnlineRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductInfo productInfo = productInfoService.getById(productOnlineRequest.getId());
        productInfo.setStatus(ProductInfoStatusEnum.ONLINE.getValue());
        boolean res = productInfoService.updateById(productInfo);
        return ResultUtils.success(res);
    }

    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineHandle(@RequestBody ProductOnlineRequest productOnlineRequest, HttpServletRequest request) {
        if (productOnlineRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductInfo productInfo = productInfoService.getById(productOnlineRequest.getId());
        productInfo.setStatus(ProductInfoStatusEnum.OFFLINE.getValue());
        boolean res = productInfoService.updateById(productInfo);
        return ResultUtils.success(res);
    }
}
