package com.nuomi.tianCaiAPI.utils;

import com.nuomi.tianCaiAPI.common.ErrorCode;
import com.nuomi.tianCaiAPI.config.EmailConfig;
import com.nuomi.tianCaiAPI.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import static com.nuomi.tianCaiAPI.constant.EmailConstant.*;

/**
 * 邮箱工具类
 * @author NuoMi
 */
@Slf4j
public class EmailUtil {

    /**
     * 生成邮件内容
     * @param emailHtmlPath 模板html文件路径
     * @param captcha 验证码
     * @return
     */
    public static String buildEmailContent(String emailHtmlPath, String captcha) {
        ClassPathResource resource = new ClassPathResource(emailHtmlPath);
        InputStream inputStream = null;
        BufferedReader fileReader = null;
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            inputStream = resource.getInputStream();
            fileReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = fileReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            log.error("读取邮箱模板失败" + e.getMessage());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return MessageFormat.format(builder.toString(), captcha, EMAIL_TITLE, EMAIL_TITLE_ENGLISH, PLATFORM_ADDRESS);
    }

    /**
     * 构建支付成功邮件
     * @param emailHtmlPath 地址
     * @param orderName 订单名称
     * @param orderTotal 金额
     * @return
     */
    public static String buildPaySuccessEmailContent(String emailHtmlPath, String orderName, String orderTotal) {
        ClassPathResource resource = new ClassPathResource(emailHtmlPath);
        InputStream inputStream = null;
        BufferedReader fileReader = null;
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            inputStream = resource.getInputStream();
            fileReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = fileReader.readLine()) != null) {
                builder.append(line);
            }
            System.out.println(builder);
        } catch (IOException e) {
            log.error("读取邮箱模板失败" + e.getMessage());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return MessageFormat.format(builder.toString(), orderName,new Integer(orderTotal) / 100, PLATFORM_RESPONSIBLE_PERSON, PATH_ADDRESS, EMAIL_TITLE);
    }

    /**
     * 发送支付成电子邮件
     *
     * @param emailAccount  用户的邮件账号
     * @param mailSender    邮件发件人
     * @param emailConfig   电子邮件配置
     * @param orderName     订单名称
     * @param orderTotal    订单金额
     * @throws MessagingException 消息传递异常
     */
    public void sendPaySuccessEmail(String emailAccount, JavaMailSender mailSender, EmailConfig emailConfig, String orderName, String orderTotal) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        // 构建发送内容
        MimeMessageHelper helper = new MimeMessageHelper(message,"UTF-8");
        helper.setSubject("【" + EMAIL_TITLE + "】感谢您的购买，请检查您的订单");
        String emailContent = buildPaySuccessEmailContent(EMAIL_HTML_PAY_SUCCESS_PATH, orderName, orderTotal);
        helper.setText(emailContent, true);
        helper.setTo(emailAccount);
        helper.setFrom(EMAIL_TITLE + '<' + emailConfig.getEmailFrom() + '>');
        mailSender.send(message);
    }
}
