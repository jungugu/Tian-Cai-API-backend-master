package com.taicai.tiancaiinterface.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ApiException;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ErrorCode;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.response.ResultResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author NuoMi
 */
@Slf4j
public class ResponseUtils {

    /**
     * 将response转为map格式
     * @param response
     * @return
     */
    public static Map<String, Object> responseToMap(String response) {
        return new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
        }.getType());
    }


    public static <T> Map<String, Object> baseResponse(String baseUrl, T params) {
        String response = null;
        try {
            response = RequestUtils.get(baseUrl, params);
            Map<String, Object> mapResponse = responseToMap(response);
            boolean success = (boolean)mapResponse.get("success");
            if (!success) {
                return mapResponse;
            }
            mapResponse.remove("success");
            return mapResponse;
        } catch (ApiException e) {
            log.error("响应异常");
            throw new RuntimeException("响应异常" + e);
        }
    }
}
