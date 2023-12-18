package com.tiancaiapi.common.service;

/**
 * @author NuoMi
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 统计调用次数
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

}
