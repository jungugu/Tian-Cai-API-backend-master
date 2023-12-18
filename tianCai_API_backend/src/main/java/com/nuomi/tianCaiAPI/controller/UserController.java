package com.nuomi.tianCaiAPI.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nuomi.tianCaiAPI.annotation.AuthCheck;
import com.nuomi.tianCaiAPI.config.EmailConfig;
import com.nuomi.tianCaiAPI.constant.UserConstant;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.exception.ThrowUtils;
import com.nuomi.tianCaiAPI.model.dto.user.*;
import com.nuomi.tianCaiAPI.model.entity.DailySignIn;
import com.nuomi.tianCaiAPI.model.vo.UserSignVO;
import com.nuomi.tianCaiAPI.service.DailySignInService;
import com.nuomi.tianCaiAPI.service.UserService;
import com.nuomi.tianCaiAPI.common.BaseResponse;
import com.nuomi.tianCaiAPI.common.DeleteRequest;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.common.ResultUtils;
import com.nuomi.tianCaiAPI.config.WxOpenConfig;
import com.nuomi.tianCaiAPI.model.vo.LoginUserVO;
import com.nuomi.tianCaiAPI.model.vo.UserVO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;

import com.nuomi.tianCaiAPI.utils.EmailUtil;
import com.tiancaiapi.common.entity.User;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.nuomi.tianCaiAPI.constant.EmailConstant.*;
import static com.nuomi.tianCaiAPI.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    @Resource
    private UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Resource
    private DailySignInService dailySignInService;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private EmailConfig emailConfig;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String invitationCode = userRegisterRequest.getInvitationCode();
        String userName = userRegisterRequest.getUserName();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userName, invitationCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/registerByEmail")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> userRegisterByEmail(@RequestBody UserRegisterByEmailRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailAccount = userRegisterRequest.getEmailAccount();
        String userName = userRegisterRequest.getUserName();
        String captcha = userRegisterRequest.getCaptcha();
        String invitationCode = userRegisterRequest.getInvitationCode();
        if (StringUtils.isAnyBlank(emailAccount, userName, captcha)) {
            return null;
        }
        long result = userService.userRegisterByEmail(emailAccount, captcha, userName, invitationCode);
        return ResultUtils.success(result);
    }


    @GetMapping("/randomSign")
    public BaseResponse<LoginUserVO> RandomSign(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userAccount = loginUser.getUserAccount();
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        // 4. 插入数据
        loginUser.setAccessKey(accessKey);
        loginUser.setSecretKey(secretKey);
        boolean updateResult = userService.updateById(loginUser);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "重新生成签名失败，数据库错误");
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(loginUser, loginUserVO);
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登录
     *
     * @param userLoginByEmailRequest
     * @param request
     * @return
     */
    @PostMapping("/loginByEmail")
    public BaseResponse<LoginUserVO> userLoginByEmail(@RequestBody UserLoginByEmailRequest userLoginByEmailRequest, HttpServletRequest request) {
        if (userLoginByEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailAccount = userLoginByEmailRequest.getEmailAccount();
        String captcha = userLoginByEmailRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", emailAccount);
        User user = userService.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String backendCaptcha = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isEmpty(backendCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码过期");
        }
        if (!backendCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = userService.getLoginUserVO(user);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登录（微信开放平台）
     */
    //@GetMapping("/login/wx_open")
    //public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request, HttpServletResponse response,
    //        @RequestParam("code") String code) {
    //    WxOAuth2AccessToken accessToken;
    //    try {
    //        WxMpService wxService = wxOpenConfig.getWxMpService();
    //        accessToken = wxService.getOAuth2Service().getAccessToken(code);
    //        WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
    //        String unionId = userInfo.getUnionId();
    //        String mpOpenId = userInfo.getOpenid();
    //        if (StringUtils.isAnyBlank(unionId, mpOpenId)) {
    //            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
    //        }
    //        return ResultUtils.success(userService.userLoginByMpOpen(userInfo, request));
    //    } catch (Exception e) {
    //        log.error("userLoginByWxOpen error", e);
    //        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
    //    }
    //}

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/signIn")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> doDailySignIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        QueryWrapper<DailySignIn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        DailySignIn signIn = dailySignInService.getOne(queryWrapper);
        DailySignIn dailySignIn = new DailySignIn();
        dailySignIn.setUserId(loginUser.getId());
        dailySignIn.setDescription("签到" + getCurrentDate());
        dailySignIn.setAddCoin(10);
        Boolean res = false;
        if (ObjectUtils.isEmpty(signIn)) {
            res = dailySignInService.save(dailySignIn);
            loginUser.setBalance(loginUser.getBalance() + dailySignIn.getAddCoin());
            Boolean updateRes = userService.updateById(loginUser);
            return ResultUtils.success(res && updateRes);
        }
        String[] split = signIn.getDescription().split("到");
        if (getCurrentDate().equals(split[1])) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "今日已签到");
        }
        dailySignIn.setId(signIn.getId());
        res = dailySignInService.updateById(dailySignIn);
        loginUser.setBalance(loginUser.getBalance() + dailySignIn.getAddCoin());
        Boolean updateRes = userService.updateById(loginUser);
        return ResultUtils.success(res && updateRes);
    }

    private String getCurrentDate() {
        // 创建一个 SimpleDateFormat 对象，指定日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 获取当前日期时间
        Date currentDate = new Date();

        // 使用 SimpleDateFormat 格式化日期
        String formattedDate = sdf.format(currentDate);

        return formattedDate;
    }

    @GetMapping("/getCaptcha")
    public BaseResponse<Boolean> getCaptcha(String emailAccount) {
        if (StringUtils.isEmpty(emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 对邮箱进行格式校验
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String captcha = RandomUtil.randomNumbers(6);
        try {
            sendEmail(emailAccount, captcha);
            redisTemplate.opsForValue().set(CAPTCHA_CACHE_KEY + emailAccount, captcha, 5, TimeUnit.MINUTES);
            return ResultUtils.success(true);
        } catch (MessagingException e) {
            log.error("发送邮件失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    private void sendEmail(String emailAccount, String captcha) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setSubject(EMAIL_SUBJECT);
        helper.setText(EmailUtil.buildEmailContent(EMAIL_HTML_CONTENT_PATH, captcha), true);
        helper.setTo(emailAccount);
        helper.setFrom(EMAIL_TITLE + '<' + emailConfig.getEmailFrom() + '>');
        mailSender.send(message);
    }

    @PostMapping("/emailBanding")
    public BaseResponse<Boolean> emailBandingHandle(@RequestBody UserEmailBindingRequest userEmailBindingRequest, HttpServletRequest request) {
        if (userEmailBindingRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailAccount = userEmailBindingRequest.getEmailAccount();
        String captcha = userEmailBindingRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", emailAccount);
        User user = userService.getOne(queryWrapper);
        if (user != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱被绑定");
        }

        if (loginUser.getEmail() != null && loginUser.getEmail().equals(emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已经绑定此邮箱，请更换邮箱");
        }
        String captchaBackend = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isEmpty(captchaBackend)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "验证码已过期");
        }
        if (!captchaBackend.equals(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        User updateuser = new User();
        updateuser.setId(loginUser.getId());
        updateuser.setEmail(emailAccount);
        boolean res = userService.updateById(updateuser);
        return ResultUtils.success(res);
    }

    @PostMapping("/unBanding")
    public BaseResponse<Boolean> emailUnBandingHandle(@RequestBody UserEmailBindingRequest userEmailBindingRequest, HttpServletRequest request) {
        if (userEmailBindingRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailAccount = userEmailBindingRequest.getEmailAccount();
        String captcha = userEmailBindingRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        String captchaBackend = redisTemplate.opsForValue().get(CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isEmpty(captchaBackend)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "验证码已过期");
        }
        if (!captchaBackend.equals(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        User updateuser = new User();
        updateuser.setId(loginUser.getId());
        updateuser.setEmail("");
        boolean res = userService.updateById(updateuser);
        return ResultUtils.success(res);
    }
}
