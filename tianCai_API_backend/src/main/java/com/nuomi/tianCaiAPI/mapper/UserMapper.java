package com.nuomi.tianCaiAPI.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiancaiapi.common.entity.User;

import java.util.List;

/**
 * 用户数据库操作
 *
 * @author NuoMi
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 获取这周注册的用户
     * @return
     */
    List<User> listUserInWeek();
}




