package com.tiancaiapi.common.service;


import com.tiancaiapi.common.entity.User;

/**
 * 用户服务
 *
 */
public interface InnerUserService {

    /**
     * 判断用户的合法
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);

}
