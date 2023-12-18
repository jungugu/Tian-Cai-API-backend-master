package com.nuomi.tianCaiAPI.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel 测试
 *

 */
@SpringBootTest
public class EasyExcelTest {

    @Test
    public void doImport() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        System.out.println(list);
    }


    @Test
    public void doDate() {
        List<String> currentWeekDates = getPreviousDays();
        System.out.println(currentWeekDates);
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

        return previousDays;
    }
}