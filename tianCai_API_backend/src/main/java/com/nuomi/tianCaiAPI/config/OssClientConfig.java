package com.nuomi.tianCaiAPI.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author NuoMi
 */
@Configuration
@ConfigurationProperties(prefix = "oss.client")
@Data
public class OssClientConfig {
    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 区域
     */
    private String endpoint;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public OSS ossClient() {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey,secretKey);
        return ossClient;
    }
}
