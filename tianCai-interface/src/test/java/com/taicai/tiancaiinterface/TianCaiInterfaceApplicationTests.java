package com.taicai.tiancaiinterface;


import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.client.TianCaiApiClient;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.exception.ApiException;

import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.params.NameParams;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.request.NameRequest;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.model.response.ResultResponse;
import com.tiancai.tiancaiapiclientsdk.tiancai_apiclientsdk.service.ApiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class TianCaiInterfaceApplicationTests {

    @Resource
    private TianCaiApiClient tianCaiApiClient;

    @Resource
    private ApiService apiService;

    @Test
    void contextLoads() {
        NameParams nameParams = new NameParams();
        nameParams.setName("nuomi");
        NameRequest nameRequest = new NameRequest();
        nameRequest.setRequestParams(nameParams);
        try {
            ResultResponse name = apiService.getName(nameRequest);
            System.out.println(name.getData());
        } catch (ApiException e) {
            System.out.println(e);
        }
    }

}
