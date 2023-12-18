package com.nuomi.tianCaiAPI.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.constant.CommonConstant;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.model.enums.UserInterfaceInfoStatusEnum;


import com.nuomi.tianCaiAPI.mapper.UserInterfaceInfoMapper;


import com.nuomi.tianCaiAPI.service.InterfaceInfoService;

import com.nuomi.tianCaiAPI.service.UserInterfaceInfoService;
import com.nuomi.tianCaiAPI.service.UserService;
import com.nuomi.tianCaiAPI.utils.SqlUtils;
import com.nuomi.tianCaiAPI.model.dto.userInterfaceInfo.UserInterfaceInfoQueryRequest;

import com.tiancaiapi.common.entity.UserInterfaceInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long interfaceId = userInterfaceInfo.getInterfaceId();
        Long userId = userInterfaceInfo.getUserId();
        Integer status = userInterfaceInfo.getStatus();
        Integer totalNum = userInterfaceInfo.getTotalNum();
        Integer leftNum = userInterfaceInfo.getLeftNum();


        if (add) {
            if (interfaceId <= 0 || interfaceInfoService.getById(interfaceId) == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口不存在");
            }
            if (userId <= 0 || userService.getById(userId) == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
            }
        }
        if (UserInterfaceInfoStatusEnum.getEnumByValue(status) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态不存在");
        }
        if (totalNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "调用次数不合法");
        }
        if (leftNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余调用次数不合法");
        }
    }


    /**
     * 获取查询包装类
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (userInterfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();
        Long id = userInterfaceInfoQueryRequest.getId();
        Long userId = userInterfaceInfoQueryRequest.getUserId();
        Long interfaceId = userInterfaceInfoQueryRequest.getInterfaceId();
        Integer status = userInterfaceInfoQueryRequest.getStatus();
        Integer totalNum = userInterfaceInfoQueryRequest.getTotalNum();
        Integer leftNum = userInterfaceInfoQueryRequest.getLeftNum();
        // 拼接查询条件
        if (UserInterfaceInfoStatusEnum.getEnumByValue(status) == null) {
            queryWrapper.eq("status",status);
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(totalNum),"totalNum",totalNum);
        queryWrapper.eq(ObjectUtils.isNotEmpty(leftNum),"leftNum",leftNum);
        queryWrapper.eq(ObjectUtils.isNotEmpty(interfaceId),"interfaceId",interfaceId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || interfaceInfoService.getById(interfaceInfoId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口不存在");
        }
        if (userId <= 0 || userService.getById(userId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceId",interfaceInfoId);
        updateWrapper.eq("userId",userId);
        updateWrapper.gt("leftNum",0);
        updateWrapper.setSql("leftNum = leftNum - 1,totalNum = totalNum + 1");
        return this.update(updateWrapper);
    }
}




