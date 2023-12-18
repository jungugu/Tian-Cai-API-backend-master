package com.nuomi.tianCaiAPI.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author NuoMi
 */
@Data
public class UserRegisterByEmailRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    private String emailAccount;

    private String userName;

    private String captcha;

    private String invitationCode;
}
