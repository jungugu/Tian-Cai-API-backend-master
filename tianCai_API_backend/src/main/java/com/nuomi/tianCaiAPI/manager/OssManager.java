package com.nuomi.tianCaiAPI.manager;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.nuomi.tianCaiAPI.config.OssClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author NuoMi
 */
@Component
public class OssManager {
    @Resource
    private OssClientConfig ossClientConfig;

    @Resource
    private OSS oss;

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        return oss.putObject(ossClientConfig.getBucket(), key, new File(localFilePath));
    }

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        return oss.putObject(ossClientConfig.getBucket(), key, file);
    }
}
