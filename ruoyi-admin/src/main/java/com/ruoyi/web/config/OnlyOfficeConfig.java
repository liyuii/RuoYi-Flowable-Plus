package com.ruoyi.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "onlyoffice")
public class OnlyOfficeConfig {
    /** OnlyOffice Document Server 地址 */
    private String docServerUrl;
    /** 应用自身地址，用于构造回调 URL 和文件下载 URL */
    private String appUrl;
    /** JWT 密钥，需与 Docker 启动时的 JWT_SECRET 一致 */
    private String jwtSecret;
}
