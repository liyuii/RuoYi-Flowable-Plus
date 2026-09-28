package com.ruoyi.demo.service;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.demo.domain.DemoUploadFile;
import com.ruoyi.demo.domain.bo.DemoUploadAuditBo;
import com.ruoyi.demo.domain.bo.DemoUploadCompleteBo;
import com.ruoyi.demo.domain.bo.DemoUploadEditBo;
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

    /**
     * 分页查询（数据隔离：只能看到自己上传的文件）
     */
    TableDataInfo<DemoUploadFile> pageUploadFiles(DemoUploadFile query, PageQuery pageQuery);

    void downloadFile(Long id, HttpServletResponse response);

    void deleteFile(Long id);

    /**
     * 编辑表单信息（只有审核不通过的文件可以编辑），保存后重新进入待审核
     */
    void editFile(DemoUploadEditBo bo);

    /**
     * 审核：通过则上传 OSS 并发布到方案库；不通过必须填原因
     */
    void auditFile(DemoUploadAuditBo bo);

}
