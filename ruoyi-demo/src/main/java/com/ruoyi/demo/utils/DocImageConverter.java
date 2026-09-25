package com.ruoyi.demo.utils;

import com.aspose.words.Document;
import com.aspose.words.ImageSaveOptions;
import com.aspose.words.SaveFormat;
import com.ruoyi.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;

/**
 * 用 Aspose.Words 把 docx 逐页渲染成 PNG。
 * <p>
 * 每页单独渲染成字节数组后立刻交给调用方处理（上传 OSS / 登记 sys_file），
 * 不会把整份文档的图片一次性堆在内存里，也不会在本地落图片文件。
 */
@Slf4j
public final class DocImageConverter {

    /**
     * 单页渲染结果的处理逻辑
     */
    @FunctionalInterface
    public interface PageHandler {

        void handle(int pageNo, byte[] png) throws Exception;
    }

    private DocImageConverter() {
    }

    /**
     * @param sourcePath 源 docx 路径
     * @param maxPages   最大转换页数，超出部分丢弃（防止超长文档把任务拖死）
     * @param resolution 渲染分辨率（dpi），预览 150 足够
     * @param handler    每页渲染完成后的处理逻辑
     * @return 实际转换的页数
     */
    public static int convert(String sourcePath, int maxPages, float resolution, PageHandler handler) {
        try {
            Document document = new Document(sourcePath);
            int totalPages = document.getPageCount();
            int limit = maxPages > 0 ? Math.min(totalPages, maxPages) : totalPages;
            if (limit < totalPages) {
                log.warn("文档页数超过上限，只转换前{}页 sourcePath={} 总页数={}", limit, sourcePath, totalPages);
            }
            for (int i = 0; i < limit; i++) {
                ImageSaveOptions options = new ImageSaveOptions(SaveFormat.PNG);
                // Aspose 的页码从 0 开始
                options.setPageIndex(i);
                options.setPageCount(1);
                options.setResolution(resolution);
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    document.save(out, options);
                    handler.handle(i + 1, out.toByteArray());
                }
            }
            return limit;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("文档转图片失败: " + e.getMessage());
        }
    }

}
