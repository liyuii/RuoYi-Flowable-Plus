package com.ruoyi.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件管理上传配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ruoyi.upload-demo")
public class DemoUploadProperties {

    /**
     * 文件存储根目录
     */
    private String storagePath;

    /**
     * 分片大小，单位字节，默认 5MB
     */
    private long chunkSize = 5 * 1024 * 1024;

}
