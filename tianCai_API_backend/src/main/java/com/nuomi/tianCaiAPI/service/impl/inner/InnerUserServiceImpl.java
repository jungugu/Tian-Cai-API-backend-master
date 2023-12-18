package com.nuomi.tianCaiAPI.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.service.UserService;
import com.tiancaiapi.common.entity.User;
import com.tiancaiapi.common.service.InnerUserService;
import org.apache.dubbo.config.annotation.DubboService;


import javax.annotation.Resource;

/**
 * @author NuoMi
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {
    @Resource
    private UserService userService;

    @Override
    public User getInvokeUser(String accessKey) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("accessKey", accessKey);
        User user = userService.getOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user;
    }
}
