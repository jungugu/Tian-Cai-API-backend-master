package com.taicai.tiancaiinterface.controller;


import cn.hutool.json.JSONUtil;
import com.taicai.tiancaiinterface.annotaion.Authority;
import com.taicai.tiancaiinterface.utils.RequestUtils;
import com.taicai.tiancaiinterface.utils.ResponseUtils;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ApiException;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ErrorCode;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.User;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.params.*;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.request.RandomAvatarRequest;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.request.RefuseClassificationRequest;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.response.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.taicai.tiancaiinterface.utils.RequestUtils.buildUrl;

/**
 * 名称 api
 *
 * @author NuoMi
 */
@RestController
public class ServiceController {

    /**
     * 获取名称接口
     * @param nameParams
     * @return
     */
    @Authority
    @GetMapping("/get")
    public NameResponse getName(NameParams nameParams) {
        return JSONUtil.toBean(JSONUtil.toJsonStr(nameParams), NameResponse.class);
    }

    /**
     * 生成随机头像接口
     * @param randomAvatarParams
     * @return
     * @throws ApiException
     */
    @Authority
    @GetMapping("/randomAvatar")
    public RandomAvatarResponse randomAvatar(RandomAvatarParams randomAvatarParams) throws ApiException {
        String baseUrl = "https://api.btstu.cn/sjbz/api.php";
        String url = buildUrl(baseUrl, randomAvatarParams);
        if (StringUtils.isAllBlank(randomAvatarParams.getLx())) {
            url = url + "?format=json";
        } else {
            url = url + "&format=json";
        }
        return JSONUtil.toBean(RequestUtils.get(url), RandomAvatarResponse.class);
    }

    /**
     * 生成随机头像接口
     * @param randomAvatarParams
     * @return
     * @throws ApiException
     */
    @Authority
    @GetMapping("/randomWallpaper")
    public RandomAvatarResponse randomWallpaper(RandomAvatarParams randomAvatarParams) throws ApiException {
        String baseUrl = "https://api.btstu.cn/sjbz/api.php";
        String url = buildUrl(baseUrl, randomAvatarParams);
        if (StringUtils.isAllBlank(randomAvatarParams.getLx())) {
            url = url + "?format=json";
        } else {
            url = url + "&format=json";
        }
        return JSONUtil.toBean(RequestUtils.get(url), RandomAvatarResponse.class);
    }

    /**
     * 随机生成毒鸡汤接口
     * @return
     * @throws ApiException
     */
    @Authority
    @GetMapping("/poisonousChickenSoup")
    public PoisonousChickenSoupResponse poisonousChickenSoup() throws ApiException {
        String baseUrl = "https://api.btstu.cn/yan/api.php?charset=utf-8&encode=json";
        return JSONUtil.toBean(RequestUtils.get(baseUrl), PoisonousChickenSoupResponse.class);
    }


    /**
     * 随机生成笑话接口
     * @return
     * @throws ApiException
     */
    @Authority
    @GetMapping("/jock")
    public JockResponse jock() throws ApiException {
        String baseUrl = "https://api.vvhan.com/api/joke?type=json";
        return JSONUtil.toBean(RequestUtils.get(baseUrl), JockResponse.class);
    }


    /**
     * 随机生成笑话接口
     * @return
     * @throws ApiException
     */
    @Authority
    @GetMapping("/refuseClassification")
    public RefuseClassificationResponse refuseClassification(RefuseClassificationParams refuseClassificationParams) throws ApiException {
        String baseUrl = "https://api.vvhan.com/api/la.ji";
        String url = buildUrl(baseUrl, refuseClassificationParams);
        return JSONUtil.toBean(RequestUtils.get(url), RefuseClassificationResponse.class);
    }

    /**
     * 随机生成笑话接口
     * @return
     * @throws ApiException
     */
    @Authority
    @GetMapping("/horoscope")
    public ResultResponse horoscope(HoroscopeParams horoscopeParams) throws ApiException {
        if (horoscopeParams == null && horoscopeParams.getType() == null && horoscopeParams.getTime() == null) {
            throw new ApiException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        String baseUrl = "https://api.vvhan.com/api/horoscope";
        String url = buildUrl(baseUrl, horoscopeParams);
        return JSONUtil.toBean(RequestUtils.get(url), ResultResponse.class);
    }
}
