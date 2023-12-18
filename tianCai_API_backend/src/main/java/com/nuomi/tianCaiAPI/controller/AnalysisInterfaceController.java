package com.nuomi.tianCaiAPI.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nuomi.tianCaiAPI.annotation.AuthCheck;
import com.nuomi.tianCaiAPI.common.BaseResponse;
import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.common.ResultUtils;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import com.nuomi.tianCaiAPI.mapper.InterfaceInfoMapper;
import com.nuomi.tianCaiAPI.mapper.ProductOrderMapper;
import com.nuomi.tianCaiAPI.mapper.UserInterfaceInfoMapper;
import com.nuomi.tianCaiAPI.mapper.UserMapper;
import com.nuomi.tianCaiAPI.model.entity.ProductOrder;
import com.nuomi.tianCaiAPI.model.vo.AnalysisOrderVO;
import com.nuomi.tianCaiAPI.model.vo.AnalysisUserRegisterVO;
import com.nuomi.tianCaiAPI.model.vo.InterfaceInfoVO;
import com.nuomi.tianCaiAPI.model.vo.UserInterfaceInfoVO;
import com.nuomi.tianCaiAPI.service.InterfaceInfoService;
import com.tiancaiapi.common.entity.InterfaceInfo;
import com.tiancaiapi.common.entity.User;
import com.tiancaiapi.common.entity.UserInterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author NuoMi
 */
@RestController
@Slf4j
@RequestMapping("/analysis")
public class AnalysisInterfaceController {
    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private ProductOrderMapper productOrderMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 分析调用数为前5的接口
     *
     * @return
     */
    @GetMapping("top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfo>> listTopInvokeInterfaceInfo(HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoMapper.listTopInvokeInterfaceInfo(5);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分析调用数为前5的接口
     *
     * @return
     */
    @GetMapping("order/total")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<AnalysisOrderVO>> listOrderInfo(HttpServletRequest request) {
        List<ProductOrder> productOrders = productOrderMapper.listOrderInWeek();
        Map<String, Long> map = new LinkedHashMap<>();

        List<String> currentWeekDates = getPreviousDays();
        // 初始化值
        for (String day : currentWeekDates) {
            map.put(day,0L);
        }
        for (ProductOrder productOrder : productOrders) {
            SimpleDateFormat sim2 = new SimpleDateFormat("yyyy-MM-dd");
            String format = sim2.format(productOrder.getCreateTime());
            if (map.containsKey(format)) {
                map.put(format, map.get(format) + productOrder.getTotal());
            }
        }
        ArrayList<AnalysisOrderVO> list = new ArrayList<>();
        for (String key : map.keySet()) {
            AnalysisOrderVO analysisOrderVO = new AnalysisOrderVO();
            analysisOrderVO.setDate(key);
            analysisOrderVO.setTotal(map.get(key) / 100);
            list.add(analysisOrderVO);
        }
        return ResultUtils.success(list);
    }


    /**
     * 分析本周注册的用户
     * @return
     */
    @GetMapping("register/user")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<AnalysisUserRegisterVO>> listRegisterUserInWeek(HttpServletRequest request) {
        List<User> userList = userMapper.listUserInWeek();
        Map<String, Integer> map = new LinkedHashMap<>();

        List<String> currentWeekDates = getPreviousDays();
        // 初始化值
        for (String day : currentWeekDates) {
            map.put(day,0);
        }
        System.out.println(currentWeekDates);

        for (User user : userList) {
            SimpleDateFormat sim2 = new SimpleDateFormat("yyyy-MM-dd");
            String format = sim2.format(user.getCreateTime());
            if (map.containsKey(format)) {
                map.put(format, map.get(format) + 1);
            }
        }
        ArrayList<AnalysisUserRegisterVO> list = new ArrayList<>();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            Integer number = map.get(key);
            AnalysisUserRegisterVO analysisUserRegisterVO = new AnalysisUserRegisterVO();
            analysisUserRegisterVO.setDate(key);
            analysisUserRegisterVO.setRegisterUserNum(number);
            list.add(analysisUserRegisterVO);
        }
        return ResultUtils.success(list);
    }

    public static List<String> getPreviousDays() {
        List<String> previousDays = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        // 格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 获取前7天的日期并添加到列表
        for (int i = 0; i < 7; i++) {
            LocalDate previousDay = currentDate.minusDays(i);
            String formattedDate = previousDay.format(formatter);
            previousDays.add(formattedDate);
        }
        Collections.reverse(previousDays);
        return previousDays;
    }
}
