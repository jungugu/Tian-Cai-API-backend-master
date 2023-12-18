package com.nuomi.tianCaiAPI.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.service.InterfaceInfoService;
import com.nuomi.tianCaiAPI.service.UserService;
import com.tiancaiapi.common.entity.InterfaceInfo;
import com.tiancaiapi.common.entity.User;
import com.tiancaiapi.common.service.InnerInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author NuoMi
 */
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path);
        queryWrapper.eq("method", method);
        InterfaceInfo interfaceInfo = interfaceInfoService.getOne(queryWrapper);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }
         return interfaceInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invokeCount(long interfaceInfoId, long userId) {
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceInfoId);
        InterfaceInfo updateInterfaceInfo = new InterfaceInfo();
        updateInterfaceInfo.setId(interfaceInfoId);
        updateInterfaceInfo.setTotalInvokes(interfaceInfo.getTotalInvokes() + 1);
        boolean res = interfaceInfoService.updateById(updateInterfaceInfo);
        User user = userService.getById(userId);
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setBalance(user.getBalance() - interfaceInfo.getReduceScore());
        boolean res2 = userService.updateById(updateUser);
        if (!res && !res2) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }
}
