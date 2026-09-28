package com.ruoyi.demo.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileDownloadUtils;
import com.ruoyi.demo.config.DemoUploadProperties;
import com.ruoyi.demo.domain.DemoUploadChunk;
import com.ruoyi.demo.domain.DemoUploadFile;
import com.ruoyi.demo.domain.bo.DemoUploadAuditBo;
import com.ruoyi.demo.domain.bo.DemoUploadCompleteBo;
import com.ruoyi.demo.domain.bo.DemoUploadEditBo;
import com.ruoyi.demo.domain.bo.DemoUploadInitBo;
import com.ruoyi.demo.domain.vo.DemoUploadSessionVo;
import com.ruoyi.demo.mapper.DemoUploadChunkMapper;
import com.ruoyi.demo.mapper.DemoUploadFileMapper;
import com.ruoyi.demo.service.IDemoUploadService;
import com.ruoyi.demo.utils.ByteArrayMultipartFile;
import com.ruoyi.demo.utils.DocImageConverter;
import com.ruoyi.demo.utils.LocalFileMultipartFile;
import com.ruoyi.doc.constant.DocSourceType;
import com.ruoyi.doc.domain.dto.DocPageDTO;
import com.ruoyi.doc.domain.dto.DocSyncDTO;
import com.ruoyi.doc.service.IDocSyncService;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件管理上传Service业务层处理
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DemoUploadServiceImpl implements IDemoUploadService {

    private static final int STATUS_UPLOADING = 0;
    private static final int STATUS_COMPLETE = 1;

    /**
     * 审核状态：待审核
     */
    private static final String AUDIT_WAIT = "0";

    /**
     * 审核状态：审核通过
     */
    private static final String AUDIT_PASS = "1";

    /**
     * 审核状态：审核不通过
     */
    private static final String AUDIT_REJECT = "2";

    /**
     * 用户上传模板在 sys_file 里的业务类型（和平台投标文件的 review_original 区分开）
     */
    private static final String BIZ_TYPE_USER_TEMPLATE = "user_template";

    /**
     * 用户上传模板转出来的页面图片在 sys_file 里的业务类型
     */
    private static final String BIZ_TYPE_USER_TEMPLATE_PAGE = "user_template_page";

    /**
     * 发布时源文件 / 页面图片的批次前缀（用于删除时按批次清理 OSS 对象）
     */
    private static final String BATCH_SOURCE_PREFIX = "user_template_";

    private static final String BATCH_PAGE_PREFIX = "user_template_page_";

    /**
     * 单个文件大小上限：50MB
     */
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private final DemoUploadFileMapper fileMapper;

    private final DemoUploadChunkMapper chunkMapper;

    private final DemoUploadProperties uploadProperties;

    private final ISysFileService sysFileService;

    private final IDocSyncService docSyncService;

    /**
     * 页面图片转换参数（复用方案库那套配置）
     */
    @Value("${ruoyi.review.image.max-pages:200}")
    private int maxPages;

    @Value("${ruoyi.review.image.resolution:150}")
    private float resolution;

    @Override
    public DemoUploadSessionVo initUpload(DemoUploadInitBo bo) {
        if (bo.getFileSize() == null || bo.getFileSize() <= 0) {
            throw new ServiceException("文件大小必须大于 0");
        }
        if (bo.getFileSize() > MAX_FILE_SIZE) {
            throw new ServiceException("文件大小不能超过 50MB");
        }
        String originalName = StringUtils.lowerCase(bo.getFileName());
        if (originalName == null || !originalName.endsWith(".docx")) {
            throw new ServiceException("只支持 .docx 格式的投标方案文件");
        }
        long chunkSize = uploadProperties.getChunkSize();
        if (chunkSize <= 0) {
            throw new ServiceException("分片大小配置错误");
        }
        int totalChunks = (int) ((bo.getFileSize() + chunkSize - 1) / chunkSize);
        if (totalChunks <= 0) {
            throw new ServiceException("分片数量计算失败");
        }

        String uploadId = UUID.randomUUID().toString().replace("-", "");
        String fileName = sanitizeFileName(bo.getFileName());
        Date now = new Date();
        if (bo.getReplaceId() != null) {
            // 重传：只有“审核不通过”的记录允许替换文件，替换后重新进入待审核
            DemoUploadFile exist = getOwnedOrThrow(bo.getReplaceId());
            if (!AUDIT_REJECT.equals(exist.getAuditStatus())) {
                throw new ServiceException("只有审核不通过的文件才能重新上传");
            }
            chunkMapper.delete(Wrappers.<DemoUploadChunk>lambdaQuery()
                .eq(DemoUploadChunk::getUploadId, exist.getUploadId()));
            cleanChunkDir(exist.getUploadId());
            deleteLocalFile(exist.getStoragePath());
            fileMapper.update(null, Wrappers.<DemoUploadFile>lambdaUpdate()
                .eq(DemoUploadFile::getId, exist.getId())
                .set(DemoUploadFile::getUploadId, uploadId)
                .set(DemoUploadFile::getFileName, fileName)
                .set(DemoUploadFile::getFileSize, bo.getFileSize())
                .set(DemoUploadFile::getFileHash, bo.getFileHash())
                .set(DemoUploadFile::getChunkSize, (int) chunkSize)
                .set(DemoUploadFile::getTotalChunks, totalChunks)
                .set(DemoUploadFile::getStatus, STATUS_UPLOADING)
                .set(DemoUploadFile::getStoragePath, null)
                .set(DemoUploadFile::getProjectName, bo.getProjectName())
                .set(DemoUploadFile::getAmount, bo.getAmount())
                .set(DemoUploadFile::getProvinceCode, bo.getProvinceCode())
                .set(DemoUploadFile::getAuditStatus, AUDIT_WAIT)
                .set(DemoUploadFile::getAuditReason, null)
                .set(DemoUploadFile::getAuditBy, null)
                .set(DemoUploadFile::getAuditTime, null)
                .set(DemoUploadFile::getDocInfoId, null)
                .set(DemoUploadFile::getPublishFileId, null)
                .set(DemoUploadFile::getUpdateTime, now));
            log.info("用户重传文件 uploadFileId={} fileName={} projectName={}",
                exist.getId(), fileName, bo.getProjectName());
        } else {
            DemoUploadFile upload = new DemoUploadFile();
            upload.setUploadId(uploadId);
            upload.setFileName(fileName);
            upload.setProjectName(bo.getProjectName());
            upload.setAmount(bo.getAmount());
            upload.setProvinceCode(bo.getProvinceCode());
            upload.setFileSize(bo.getFileSize());
            upload.setFileHash(bo.getFileHash());
            upload.setChunkSize((int) chunkSize);
            upload.setTotalChunks(totalChunks);
            upload.setStatus(STATUS_UPLOADING);
            upload.setAuditStatus(AUDIT_WAIT);
            upload.setDeleted(0);
            upload.setCreateBy(LoginHelper.getUsername());
            upload.setCreateTime(now);
            upload.setUpdateTime(now);
            fileMapper.insert(upload);
        }

        DemoUploadSessionVo vo = new DemoUploadSessionVo();
        vo.setUploadId(uploadId);
        vo.setChunkSize((int) chunkSize);
        vo.setTotalChunks(totalChunks);
        vo.setUploadedChunkIndexes(new ArrayList<>());
        return vo;
    }

    @Override
    public DemoUploadSessionVo getProgress(String fileHash) {
        if (StringUtils.isBlank(fileHash)) {
            return null;
        }
        DemoUploadFile upload = fileMapper.selectOne(Wrappers.<DemoUploadFile>lambdaQuery()
            .eq(DemoUploadFile::getFileHash, fileHash)
            .eq(DemoUploadFile::getStatus, STATUS_UPLOADING)
            .eq(DemoUploadFile::getDeleted, 0)
            .eq(DemoUploadFile::getCreateBy, LoginHelper.getUsername())
            .orderByDesc(DemoUploadFile::getId)
            .last("LIMIT 1"));
        if (upload == null) {
            return null;
        }
        return buildSessionVo(upload);
    }

    @Override
    public void uploadChunk(String uploadId, Integer chunkIndex, MultipartFile file) {
        DemoUploadFile upload = getUploadingOrThrow(uploadId);
        if (chunkIndex == null || chunkIndex < 0 || chunkIndex >= upload.getTotalChunks()) {
            throw new ServiceException("分片序号不合法");
        }
        if (file == null || file.isEmpty()) {
            throw new ServiceException("分片内容不能为空");
        }

        Path chunkDir = chunkDir(uploadId);
        Path target = chunkDir.resolve(chunkIndex + ".part");
        DemoUploadChunk dbChunk = chunkMapper.selectOne(Wrappers.<DemoUploadChunk>lambdaQuery()
            .eq(DemoUploadChunk::getUploadId, uploadId)
            .eq(DemoUploadChunk::getChunkIndex, chunkIndex));
        if (dbChunk != null && Files.exists(target)) {
            return;
        }

        try {
            Files.createDirectories(chunkDir);
            Path temp = chunkDir.resolve(chunkIndex + ".part.tmp");
            Files.deleteIfExists(temp);
            try (InputStream in = file.getInputStream();
                 OutputStream out = Files.newOutputStream(temp,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                copyStream(in, out);
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            saveChunkRecord(uploadId, chunkIndex, file.getSize());
        } catch (Exception e) {
            log.error("保存分片失败 uploadId={}, chunkIndex={}", uploadId, chunkIndex, e);
            throw new ServiceException("分片保存失败: " + e.getMessage());
        }
    }

    @Override
    public Long completeUpload(DemoUploadCompleteBo bo) {
        DemoUploadFile upload = getUploadingOrThrow(bo.getUploadId());
        if (upload.getTotalChunks() == null || upload.getTotalChunks() <= 0) {
            throw new ServiceException("上传任务数据异常");
        }
        List<DemoUploadChunk> chunks = chunkMapper.selectList(Wrappers.<DemoUploadChunk>lambdaQuery()
            .eq(DemoUploadChunk::getUploadId, bo.getUploadId())
            .eq(DemoUploadChunk::getStatus, 1));
        if (chunks.size() < upload.getTotalChunks()) {
            throw new ServiceException("分片未传完，当前 " + chunks.size() + "/" + upload.getTotalChunks());
        }

        Map<Integer, DemoUploadChunk> chunkMap = new HashMap<>();
        long totalSize = 0;
        for (DemoUploadChunk chunk : chunks) {
            chunkMap.put(chunk.getChunkIndex(), chunk);
            totalSize += chunk.getChunkSize() == null ? 0 : chunk.getChunkSize();
        }
        for (int i = 0; i < upload.getTotalChunks(); i++) {
            DemoUploadChunk chunk = chunkMap.get(i);
            if (chunk == null) {
                throw new ServiceException("缺少分片: " + i);
            }
            if (!Files.exists(chunkPath(bo.getUploadId(), i))) {
                throw new ServiceException("分片文件不存在，请重传分片: " + i);
            }
        }
        if (totalSize != upload.getFileSize()) {
            throw new ServiceException("分片总大小与文件大小不一致");
        }

        try {
            Path fileDir = fileDir();
            Files.createDirectories(fileDir);
            String safeName = sanitizeFileName(upload.getFileName());
            Path finalPath = fileDir.resolve(upload.getUploadId() + "_" + safeName);
            try (OutputStream out = Files.newOutputStream(finalPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (int i = 0; i < upload.getTotalChunks(); i++) {
                    Files.copy(chunkPath(bo.getUploadId(), i), out);
                }
            }

            String actualHash = sha256(finalPath);
            String expectedHash = StringUtils.isNotBlank(bo.getFileHash())
                ? bo.getFileHash()
                : upload.getFileHash();
            if (StringUtils.isNotBlank(expectedHash) && !expectedHash.equalsIgnoreCase(actualHash)) {
                Files.deleteIfExists(finalPath);
                throw new ServiceException("文件哈希校验失败，请重新上传");
            }

            upload.setStatus(STATUS_COMPLETE);
            upload.setStoragePath(finalPath.toString());
            upload.setUpdateTime(new Date());
            fileMapper.updateById(upload);

            cleanChunkDir(bo.getUploadId());
            return upload.getId();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("合并文件失败 uploadId={}", bo.getUploadId(), e);
            throw new ServiceException("文件合并失败: " + e.getMessage());
        }
    }

    @Override
    public void cancelUpload(String uploadId) {
        DemoUploadFile upload = getUploadingOrThrow(uploadId);
        chunkMapper.delete(Wrappers.<DemoUploadChunk>lambdaQuery()
            .eq(DemoUploadChunk::getUploadId, uploadId));
        fileMapper.deleteById(upload.getId());
        cleanChunkDir(uploadId);
    }

    @Override
    public TableDataInfo<DemoUploadFile> pageUploadFiles(DemoUploadFile query, PageQuery pageQuery) {
        DemoUploadFile condition = query == null ? new DemoUploadFile() : query;
        LambdaQueryWrapper<DemoUploadFile> wrapper = Wrappers.<DemoUploadFile>lambdaQuery()
            .eq(DemoUploadFile::getStatus, STATUS_COMPLETE)
            .eq(DemoUploadFile::getDeleted, 0)
            // 数据隔离：只能看到自己上传的文件
            .eq(DemoUploadFile::getCreateBy, LoginHelper.getUsername())
            .like(StringUtils.isNotBlank(condition.getProjectName()),
                DemoUploadFile::getProjectName, condition.getProjectName())
            .eq(StringUtils.isNotBlank(condition.getProvinceCode()),
                DemoUploadFile::getProvinceCode, condition.getProvinceCode())
            .eq(StringUtils.isNotBlank(condition.getAuditStatus()),
                DemoUploadFile::getAuditStatus, condition.getAuditStatus())
            .orderByDesc(DemoUploadFile::getId);
        Page<DemoUploadFile> page = fileMapper.selectPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public void downloadFile(Long id, HttpServletResponse response) {
        DemoUploadFile upload = getOwnedOrThrow(id);
        if (!Integer.valueOf(STATUS_COMPLETE).equals(upload.getStatus())
            || StringUtils.isBlank(upload.getStoragePath())) {
            throw new ServiceException("文件不存在或已删除");
        }
        Path path = new File(upload.getStoragePath()).toPath();
        if (!Files.exists(path)) {
            throw new ServiceException("文件已从磁盘丢失");
        }
        long start = System.currentTimeMillis();
        log.info("开始下载用户文件 id={} fileName={} size={}B", id, upload.getFileName(), upload.getFileSize());
        try {
            FileDownloadUtils.writeResponse(upload.getFileName(), Files.size(path),
                () -> Files.newInputStream(path), response);
            log.info("用户文件下载完成 id={} fileName={} cost={}ms",
                id, upload.getFileName(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("下载文件失败 id={}", id, e);
            throw new ServiceException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(Long id) {
        DemoUploadFile upload = getOwnedOrThrow(id);
        fileMapper.update(null, Wrappers.<DemoUploadFile>lambdaUpdate()
            .eq(DemoUploadFile::getId, id)
            .set(DemoUploadFile::getDeleted, 1)
            .set(DemoUploadFile::getUpdateTime, new Date()));
        deleteLocalFile(upload.getStoragePath());
        // 已经审核通过、发布到“投标方案列表”的，连带把文档库记录和 OSS 文件清掉
        if (upload.getDocInfoId() != null) {
            try {
                docSyncService.removeBySource(DocSourceType.USER_TEMPLATE, upload.getId());
                log.info("删除用户文件时同步删除文档库记录 uploadFileId={} docInfoId={}",
                    id, upload.getDocInfoId());
            } catch (Exception e) {
                log.warn("删除文档库记录失败 uploadFileId={} docInfoId={} reason={}",
                    id, upload.getDocInfoId(), e.getMessage());
            }
        }
        // 源文件 + 页面图片都按批次清理（sys_file 记录 + OSS 对象）
        deleteFilesByBatch(BATCH_SOURCE_PREFIX + id);
        deleteFilesByBatch(BATCH_PAGE_PREFIX + id);
        log.info("用户文件删除完成 id={} fileName={}", id, upload.getFileName());
    }

    @Override
    public void editFile(DemoUploadEditBo bo) {
        DemoUploadFile upload = getOwnedOrThrow(bo.getId());
        if (!AUDIT_REJECT.equals(upload.getAuditStatus())) {
            throw new ServiceException("只有审核不通过的文件才能编辑");
        }
        fileMapper.update(null, Wrappers.<DemoUploadFile>lambdaUpdate()
            .eq(DemoUploadFile::getId, upload.getId())
            .set(DemoUploadFile::getProjectName, bo.getProjectName())
            .set(DemoUploadFile::getAmount, bo.getAmount())
            .set(DemoUploadFile::getProvinceCode, bo.getProvinceCode())
            .set(DemoUploadFile::getAuditStatus, AUDIT_WAIT)
            .set(DemoUploadFile::getAuditReason, null)
            .set(DemoUploadFile::getAuditBy, null)
            .set(DemoUploadFile::getAuditTime, null)
            .set(DemoUploadFile::getUpdateTime, new Date()));
        log.info("用户编辑文件信息并重新提交审核 uploadFileId={} projectName={}",
            upload.getId(), bo.getProjectName());
    }

    @Override
    public void auditFile(DemoUploadAuditBo bo) {
        DemoUploadFile upload = getOwnedOrThrow(bo.getId());
        if (!Integer.valueOf(STATUS_COMPLETE).equals(upload.getStatus())) {
            throw new ServiceException("文件还没上传完成，不能审核");
        }
        if (!AUDIT_WAIT.equals(upload.getAuditStatus())) {
            throw new ServiceException("该文件已经审核过了");
        }
        String operName = LoginHelper.getUsername();
        Date now = new Date();
        if (Boolean.TRUE.equals(bo.getPass())) {
            PublishResult result = publishToDocLibrary(upload);
            fileMapper.update(null, Wrappers.<DemoUploadFile>lambdaUpdate()
                .eq(DemoUploadFile::getId, upload.getId())
                .set(DemoUploadFile::getAuditStatus, AUDIT_PASS)
                .set(DemoUploadFile::getAuditReason, null)
                .set(DemoUploadFile::getAuditBy, operName)
                .set(DemoUploadFile::getAuditTime, now)
                .set(DemoUploadFile::getDocInfoId, result.docInfoId)
                .set(DemoUploadFile::getPublishFileId, result.sysFileId)
                .set(DemoUploadFile::getUpdateTime, now));
            log.info("用户上传模板审核通过并发布到投标方案列表 uploadFileId={} docInfoId={} projectName={}",
                upload.getId(), result.docInfoId, upload.getProjectName());
        } else {
            if (StringUtils.isBlank(bo.getReason())) {
                throw new ServiceException("审核不通过必须填写原因");
            }
            fileMapper.update(null, Wrappers.<DemoUploadFile>lambdaUpdate()
                .eq(DemoUploadFile::getId, upload.getId())
                .set(DemoUploadFile::getAuditStatus, AUDIT_REJECT)
                .set(DemoUploadFile::getAuditReason, StringUtils.substring(bo.getReason(), 0, 500))
                .set(DemoUploadFile::getAuditBy, operName)
                .set(DemoUploadFile::getAuditTime, now)
                .set(DemoUploadFile::getUpdateTime, now));
            log.info("用户上传模板审核不通过 uploadFileId={} projectName={} reason={}",
                upload.getId(), upload.getProjectName(), bo.getReason());
        }
    }

    /**
     * 审核通过：本地文件上传 OSS + 登记 sys_file → 逐页转成预览图片并登记 sys_file
     * → 带着页面清单直接发布到文档库（投标方案列表）。
     * 用户上传的模板是“可直接使用”的，不参与拆分/识别/脱敏/转图片，和平台脱敏文件是两条独立链路。
     */
    private PublishResult publishToDocLibrary(DemoUploadFile upload) {
        if (StringUtils.isBlank(upload.getStoragePath())) {
            throw new ServiceException("本地文件不存在，请重新上传");
        }
        File local = new File(upload.getStoragePath());
        if (!local.exists()) {
            throw new ServiceException("本地文件不存在，请重新上传");
        }
        long start = System.currentTimeMillis();
        String operName = LoginHelper.getUsername();
        // 重新发布时先清掉上一版发布出来的文件（源文件 + 页面图片），避免 OSS 残留
        deleteFilesByBatch(BATCH_SOURCE_PREFIX + upload.getId());
        deleteFilesByBatch(BATCH_PAGE_PREFIX + upload.getId());

        SysFile sourceFile = null;
        List<SysFile> pageFiles = new ArrayList<>();
        try {
            sourceFile = sysFileService.upload(
                new LocalFileMultipartFile(local, upload.getFileName()),
                BIZ_TYPE_USER_TEMPLATE, BATCH_SOURCE_PREFIX + upload.getId(), operName);
            // 逐页转图片（同步）：文档库列表的首页缩略图、详情页逐页预览、页数都用这批图片
            String baseName = baseName(upload.getFileName());
            String pageBatchId = BATCH_PAGE_PREFIX + upload.getId();
            DocImageConverter.convert(local.getAbsolutePath(), maxPages, resolution, (pageNo, png) -> {
                SysFile pageFile = sysFileService.upload(
                    new ByteArrayMultipartFile(png, baseName + "-第" + pageNo + "页.png", "image/png"),
                    BIZ_TYPE_USER_TEMPLATE_PAGE, pageBatchId, operName);
                pageFiles.add(pageFile);
            });
            if (pageFiles.isEmpty()) {
                throw new ServiceException("文件没有可预览的页面");
            }
            DocSyncDTO dto = new DocSyncDTO();
            dto.setSourceType(DocSourceType.USER_TEMPLATE);
            dto.setSourceBizId(upload.getId());
            dto.setDocName(upload.getFileName());
            dto.setProjectName(upload.getProjectName());
            dto.setProvinceCode(upload.getProvinceCode());
            dto.setAmount(upload.getAmount());
            dto.setFileUrl(sourceFile.getOssUrl());
            dto.setFileSize(sourceFile.getFileSize());
            List<DocPageDTO> pages = new ArrayList<>(pageFiles.size());
            int pageNo = 0;
            for (SysFile pageFile : pageFiles) {
                DocPageDTO page = new DocPageDTO();
                page.setPageNo(++pageNo);
                page.setFileUrl(pageFile.getOssUrl());
                page.setFileSize(pageFile.getFileSize());
                pages.add(page);
            }
            dto.setPages(pages);
            Long docInfoId = docSyncService.sync(dto);
            log.info("用户模板发布到投标方案列表完成 uploadFileId={} sysFileId={} docInfoId={} 页数={} size={}B cost={}ms",
                upload.getId(), sourceFile.getId(), docInfoId, pages.size(), local.length(),
                System.currentTimeMillis() - start);
            return new PublishResult(docInfoId, sourceFile.getId());
        } catch (Exception e) {
            // 系统异常：清掉这次已经传上去的文件，审核状态保持不变，提示用户重新提交
            log.warn("用户模板发布失败，已清理本次上传的文件 uploadFileId={} reason={}",
                upload.getId(), e.getMessage());
            if (sourceFile != null) {
                deleteFileQuietly(sourceFile.getId());
            }
            for (SysFile pageFile : pageFiles) {
                deleteFileQuietly(pageFile.getId());
            }
            throw new ServiceException("文件预览生成失败，请更换文件后重新提交审核：" + e.getMessage());
        }
    }

    /**
     * 发布结果：文档库记录ID（doc_info.id）+ OSS 文件ID（sys_file.id）
     */
    private static class PublishResult {

        private final Long docInfoId;

        private final Long sysFileId;

        private PublishResult(Long docInfoId, Long sysFileId) {
            this.docInfoId = docInfoId;
            this.sysFileId = sysFileId;
        }
    }

    /**
     * 取自己上传的文件，顺带做数据隔离校验
     */
    private DemoUploadFile getOwnedOrThrow(Long id) {
        DemoUploadFile upload = fileMapper.selectById(id);
        if (upload == null || Integer.valueOf(1).equals(upload.getDeleted())) {
            throw new ServiceException("文件不存在或已删除");
        }
        if (!StringUtils.equals(LoginHelper.getUsername(), upload.getCreateBy())) {
            throw new ServiceException("只能操作自己上传的文件");
        }
        return upload;
    }

    private void deleteLocalFile(String storagePath) {
        if (StringUtils.isBlank(storagePath)) {
            return;
        }
        try {
            Files.deleteIfExists(new File(storagePath).toPath());
        } catch (Exception e) {
            log.warn("删除本地文件失败 path={} reason={}", storagePath, e.getMessage());
        }
    }

    /**
     * 按批次删除文件（sys_file 记录 + OSS 对象），单个删除失败不影响其它文件
     */
    private void deleteFilesByBatch(String batchId) {
        for (SysFile file : sysFileService.listByBatch(batchId)) {
            deleteFileQuietly(file.getId());
        }
    }

    private void deleteFileQuietly(Long fileId) {
        try {
            sysFileService.deleteById(fileId);
        } catch (Exception e) {
            log.warn("删除 OSS 文件失败 fileId={} reason={}", fileId, e.getMessage());
        }
    }

    /**
     * 页面图片文件名去掉扩展名，用于拼“xxx-第N页.png”
     */
    private String baseName(String fileName) {
        String name = StringUtils.isBlank(fileName) ? "投标方案" : fileName;
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private DemoUploadFile getUploadingOrThrow(String uploadId) {
        DemoUploadFile upload = fileMapper.selectOne(Wrappers.<DemoUploadFile>lambdaQuery()
            .eq(DemoUploadFile::getUploadId, uploadId)
            .eq(DemoUploadFile::getStatus, STATUS_UPLOADING)
            .eq(DemoUploadFile::getDeleted, 0));
        if (upload == null) {
            throw new ServiceException("上传任务不存在或已取消");
        }
        return upload;
    }

    private void saveChunkRecord(String uploadId, int chunkIndex, long chunkSize) throws Exception {
        Date now = new Date();
        DemoUploadChunk chunk = new DemoUploadChunk();
        chunk.setUploadId(uploadId);
        chunk.setChunkIndex(chunkIndex);
        chunk.setChunkSize(chunkSize);
        chunk.setStatus(1);
        chunk.setCreateTime(now);
        chunk.setUpdateTime(now);
        try {
            chunkMapper.insert(chunk);
        } catch (DuplicateKeyException e) {
            DemoUploadChunk update = new DemoUploadChunk();
            update.setChunkSize(chunkSize);
            update.setUpdateTime(now);
            chunkMapper.update(update, Wrappers.<DemoUploadChunk>lambdaUpdate()
                .eq(DemoUploadChunk::getUploadId, uploadId)
                .eq(DemoUploadChunk::getChunkIndex, chunkIndex));
        }
    }

    private DemoUploadSessionVo buildSessionVo(DemoUploadFile upload) {
        List<DemoUploadChunk> chunks = chunkMapper.selectList(Wrappers.<DemoUploadChunk>lambdaQuery()
            .eq(DemoUploadChunk::getUploadId, upload.getUploadId())
            .eq(DemoUploadChunk::getStatus, 1)
            .orderByAsc(DemoUploadChunk::getChunkIndex));
        List<Integer> uploaded = new ArrayList<>();
        for (DemoUploadChunk chunk : chunks) {
            uploaded.add(chunk.getChunkIndex());
        }
        DemoUploadSessionVo vo = new DemoUploadSessionVo();
        vo.setUploadId(upload.getUploadId());
        vo.setChunkSize(upload.getChunkSize());
        vo.setTotalChunks(upload.getTotalChunks());
        vo.setUploadedChunkIndexes(uploaded);
        return vo;
    }

    private Path rootDir() {
        String path = uploadProperties.getStoragePath();
        if (StringUtils.isBlank(path)) {
            throw new ServiceException("未配置文件存储目录");
        }
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new ServiceException("创建文件存储目录失败: " + path);
        }
        return dir.toPath();
    }

    private Path chunkDir(String uploadId) {
        return rootDir().resolve("tmp").resolve(uploadId);
    }

    private Path chunkPath(String uploadId, int chunkIndex) {
        return chunkDir(uploadId).resolve(chunkIndex + ".part");
    }

    private Path fileDir() {
        return rootDir().resolve("files");
    }

    private void cleanChunkDir(String uploadId) {
        FileUtil.del(chunkDir(uploadId).toFile());
    }

    private String sanitizeFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "unnamed";
        }
        String name = fileName.trim()
            .replace("\\", "_")
            .replace("/", "_")
            .replaceAll("[\\\\/:*?\"<>|]", "_");
        return StringUtils.isBlank(name) ? "unnamed" : name;
    }

    private String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        try (InputStream in = Files.newInputStream(path)) {
            int len;
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private void copyStream(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

}
