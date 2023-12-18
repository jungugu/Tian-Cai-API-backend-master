package com.nuomi.tianCaiAPI.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * 绑定邮箱请求参数
 * @author NuoMi
 */
@Data
public class UserEmailBindingRequest implements Serializable {
    private String emailAccount;
    private String captcha;
}
