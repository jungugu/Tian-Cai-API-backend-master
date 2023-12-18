package com.nuomi.tianCaiAPI.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiancaiapi.common.entity.InterfaceInfo;
import com.tiancaiapi.common.entity.UserInterfaceInfo;

import java.util.List;

/**
 * @Entity com.nuomi.tianCaiAPI.model.entity.InterfaceInfo
 */
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {
    List<InterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}




