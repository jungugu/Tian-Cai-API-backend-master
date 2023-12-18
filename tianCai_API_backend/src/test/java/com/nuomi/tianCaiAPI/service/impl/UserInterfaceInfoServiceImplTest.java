package com.nuomi.tianCaiAPI.service.impl;





import com.nuomi.tianCaiAPI.service.UserInterfaceInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @author NuoMi
 */
@SpringBootTest
public class UserInterfaceInfoServiceImplTest {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Test
    public void invokeCount() {
        boolean res = userInterfaceInfoService.invokeCount(1, 1699615963988664321L);
        Assertions.assertTrue(res);
    }
}