package com.tiancaiapi.common.service;


import com.tiancaiapi.common.entity.InterfaceInfo;

/**
 *
 * @author NuoMi
 */
public interface InnerInterfaceInfoService {
    /**
     * 判断接口是否存在
     * @param path
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String path, String method);

    boolean invokeCount(long interfaceInfoId, long userId);
}
