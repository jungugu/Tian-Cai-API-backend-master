package com.nuomi.tianCaiAPI.service.impl;

import static com.nuomi.tianCaiAPI.constant.EmailConstant.CAPTCHA_CACHE_KEY;
import static com.nuomi.tianCaiAPI.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuomi.tianCaiAPI.service.UserService;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.constant.CommonConstant;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.mapper.UserMapper;
import com.nuomi.tianCaiAPI.model.dto.user.UserQueryRequest;
import com.nuomi.tianCaiAPI.model.enums.UserRoleEnum;
import com.nuomi.tianCaiAPI.model.vo.LoginUserVO;
import com.nuomi.tianCaiAPI.model.vo.UserVO;
import com.nuomi.tianCaiAPI.utils.SqlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.tiancaiapi.common.entity.User;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *

 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword ,String userName, String inviteCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3.分配 accessKey和 secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            String invitationCode = randomInvitationCode();
            user.setInvitationCode(invitationCode);
            user.setUserName(userName);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            // 执行邀请
            invite(inviteCode, user.getId());
            return user.getId();
        }
    }

    @Override
    public long userRegisterByEmail(String emailAccount, String captcha, String userName, String inviteCode) {
        if (StringUtils.isAnyEmpty(emailAccount, captcha, userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", emailAccount);
        User user = this.getOne(wrapper);
        if (!ObjectUtils.isEmpty(user)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册请更换");
        }
        String backendCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isEmpty(backendCaptcha)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "验证码过期");
        }
        if (!backendCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        // 3.分配 accessKey和 secretKey
        String accessKey = DigestUtil.md5Hex(SALT + emailAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + emailAccount + RandomUtil.randomNumbers(8));

        User saveUser = new User();
        saveUser.setUserName(userName);
        saveUser.setEmail(emailAccount);
        saveUser.setUserAccount(emailAccount);
        saveUser.setAccessKey(accessKey);
        saveUser.setSecretKey(secretKey);
        String invitationCode = randomInvitationCode();
        saveUser.setInvitationCode(invitationCode);
        boolean res = this.save(saveUser);
        if (!res) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库保存失败");
        }
        invite(inviteCode, saveUser.getId());
        return saveUser.getId();
    }

    public boolean invite(String inviteCode, Long userId) {
        if (StringUtils.isEmpty(inviteCode)) {
            return false;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("invitationCode", inviteCode);
        User user = getOne(queryWrapper);
        user.setBalance(user.getBalance() + 100);
        boolean update1 = updateById(user);
        User inviteUser = getById(userId);
        inviteUser.setBalance(100L + 100L);
        boolean update = updateById(inviteUser);
        if (!(update && update1)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邀请失败");
        }
        return update1 && update;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    //@Override
    //public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
    //    String unionId = wxOAuth2UserInfo.getUnionId();
    //    String mpOpenId = wxOAuth2UserInfo.getOpenid();
    //    // 单机锁
    //    synchronized (unionId.intern()) {
    //        // 查询用户是否已存在
    //        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    //        queryWrapper.eq("unionId", unionId);
    //        User user = this.getOne(queryWrapper);
    //        // 被封号，禁止登录
    //        if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
    //            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
    //        }
    //        // 用户不存在则创建
    //        if (user == null) {
    //            user = new User();
    //            user.setUnionId(unionId);
    //            user.setMpOpenId(mpOpenId);
    //            user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
    //            user.setUserName(wxOAuth2UserInfo.getNickname());
    //            boolean result = this.save(user);
    //            if (!result) {
    //                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
    //            }
    //        }
    //        // 记录用户的登录态
    //        request.getSession().setAttribute(USER_LOGIN_STATE, user);
    //        return getLoginUserVO(user);
    //    }
    //}

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public String randomInvitationCode() {
        String invitationCode = RandomUtil.randomNumbers(6);
        return invitationCode;
    }

    @Override
    public User isTourist(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getId() == null) {
            return null;
        }
        Long id = user.getId();
        return this.getById(id);
    }

    @Override
    public boolean addCoinBalance(Long userId, Long addCoins) {
        if (addCoins < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = this.getById(userId);
        if (ObjectUtils.isEmpty(user)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到对应用户");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setBalance(user.getBalance() + addCoins);
        return this.updateById(updateUser);
    }
}
