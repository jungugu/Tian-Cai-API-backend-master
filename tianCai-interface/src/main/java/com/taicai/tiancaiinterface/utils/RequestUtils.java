package com.taicai.tiancaiinterface.utils;

import cn.hutool.http.HttpRequest;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ApiException;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 发出请求
 * @author NuoMi
 */
@Slf4j
public class RequestUtils {

    /**
     * 构建有参数的url
     * @param baseUrl
     * @param params
     * @return
     * @param <T>
     * @throws ApiException
     */
    public static <T> String buildUrl(String baseUrl, T params) throws ApiException {
        StringBuilder url = new StringBuilder(baseUrl);
        Field[] fields = params.getClass().getDeclaredFields();
        boolean isFirstField = true;
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            if ("serialVersionUID".equals(name)) {
                continue;
            }
            try {
                Object value = field.get(params);
                if (value != null) {
                    if (isFirstField) {
                        isFirstField = false;
                        url.append("?").append(name).append("=").append(value);
                    } else {
                        url.append("&").append(name).append("=").append(value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new ApiException(ErrorCode.OPERATION_ERROR, "构建url异常");
            }
        }
        return url.toString();
    }

    /**
     * get有参请求
     * @param baseUrl
     * @param params
     * @return
     * @param <T>
     * @throws ApiException
     */
    public static <T> String get(String baseUrl, T params) throws ApiException {
        return get(buildUrl(baseUrl, params));
    }

    /**
     * get 无参请求
     * @param url
     * @return
     */
    public static String get(String url) {
        String body = HttpRequest.get(url).execute().body();
        log.info("【interface】：请求地址：{}，响应数据：{}", url, body);
        return body;
    }
}
