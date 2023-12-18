package com.nuomi.tianCaiAPI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nuomi.tianCaiAPI.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tiancaiapi.common.entity.InterfaceInfo;

/**
 *
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);


    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);
}
