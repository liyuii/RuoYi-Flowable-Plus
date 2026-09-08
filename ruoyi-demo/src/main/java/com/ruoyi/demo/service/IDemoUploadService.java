package com.ruoyi.demo.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.demo.domain.DemoUploadFile;
import com.ruoyi.demo.domain.bo.DemoUploadCompleteBo;
import com.ruoyi.demo.domain.bo.DemoUploadInitBo;
import com.ruoyi.demo.domain.vo.DemoUploadSessionVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 文件管理上传Service接口
 */
public interface IDemoUploadService {

    DemoUploadSessionVo initUpload(DemoUploadInitBo bo);

    DemoUploadSessionVo getProgress(String fileHash);

    void uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file);

    Long completeUpload(DemoUploadCompleteBo bo);

    void cancelUpload(String uploadId);

    TableDataInfo<DemoUploadFile> pageUploadFiles(PageQuery pageQuery);

    void downloadFile(Long id, HttpServletResponse response);

    void deleteFile(Long id);

}
