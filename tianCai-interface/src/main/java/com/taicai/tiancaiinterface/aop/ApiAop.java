package com.taicai.tiancaiinterface.aop;

import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ApiException;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author NuoMi
 */
@Aspect
@Component
@Slf4j
public class ApiAop {

    @Pointcut("@annotation(com.taicai.tiancaiinterface.annotaion.Authority)")
    private void pointcut() {};

    @Before("pointcut()")
    public void beforeAdvice(JoinPoint joinPoint) throws ApiException {
        log.info("接口执行之前");
        Signature signature = joinPoint.getSignature();
        // 获取切入的包名
        String declaringTypeName = signature.getDeclaringTypeName();
        // 获取即将执行的方法名
        String funcName = signature.getName();
        log.info("即将执行方法为: {}，属于{}包", funcName, declaringTypeName);

        // 也可以用来记录一些信息，比如获取请求的 URL 和 IP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String value = attributes.getRequest().getHeader("nuomi");
        if (!(StringUtils.isNotBlank(value) && value.equals("666"))) {
            log.error("流量未染色");
            throw new ApiException(ErrorCode.NO_AUTH_ERROR, "流量未染色");
        }
        log.info("流量染色");
    }
}
