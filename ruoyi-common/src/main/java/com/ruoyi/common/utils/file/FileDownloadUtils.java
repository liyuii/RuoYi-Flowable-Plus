package com.ruoyi.common.utils.file;

import cn.hutool.core.io.IoUtil;
import com.ruoyi.common.exception.ServiceException;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载响应处理：本地文件、OSS 流都可以复用这一套响应头与拷贝逻辑。
 */
public final class FileDownloadUtils {

    /**
     * 下载流来源（本地文件或 OSS），允许抛受检异常
     */
    @FunctionalInterface
    public interface StreamSupplier {

        InputStream get() throws Exception;
    }

    private FileDownloadUtils() {
    }

    /**
     * 把文件流写回浏览器，文件名按 RFC 5987 编码，中文名不会乱码
     */
    public static void writeResponse(String fileName, Long size,
                                     StreamSupplier streamSupplier, HttpServletResponse response) {
        try {
            String asciiName = fileName.replaceAll("[^\\x20-\\x7E]", "_");
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedName);
            if (size != null && size > 0) {
                response.setContentLengthLong(size);
            }
            try (InputStream in = streamSupplier.get();
                 OutputStream out = response.getOutputStream()) {
                IoUtil.copy(in, out);
                out.flush();
            }
        } catch (Exception e) {
            throw new ServiceException("文件下载失败: " + e.getMessage());
        }
    }

}
