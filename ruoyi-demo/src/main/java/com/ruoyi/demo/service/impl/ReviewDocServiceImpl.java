package com.ruoyi.demo.service.impl;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.domain.vo.ReviewContentVO;
import com.ruoyi.demo.domain.vo.ReviewNodeVO;
import com.ruoyi.demo.domain.vo.ReviewRecognizeVO;
import com.ruoyi.demo.mapper.ReviewDocMapper;
import com.ruoyi.demo.mapper.ReviewSpanMapper;
import com.ruoyi.demo.service.IReviewDocService;
import com.ruoyi.demo.service.ReviewSensitiveRecognizer;
import com.ruoyi.demo.utils.LocalFileMultipartFile;
import com.ruoyi.demo.utils.ReviewChapterSplitter;
import com.ruoyi.demo.utils.ReviewDocxParser;
import com.ruoyi.demo.utils.ReviewMaskApplier;
import com.ruoyi.oss.service.FileStorageStrategy;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 人工审核Service业务层处理
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewDocServiceImpl implements IReviewDocService {

    /**
     * 流程状态：待拆分
     */
    private static final String PROCESS_WAIT_SPLIT = "WAIT_SPLIT";

    /**
     * 流程状态：已拆分
     */
    private static final String PROCESS_SPLIT_DONE = "SPLIT_DONE";

    /**
     * 流程状态：待审核（识别完成，等待人工确认敏感词）
     */
    private static final String PROCESS_WAIT_REVIEW = "WAIT_REVIEW";

    /**
     * 流程状态：待脱敏（审核已完成，但脱敏失败，可重试）
     */
    private static final String PROCESS_WAIT_MASK = "WAIT_MASK";

    /**
     * 流程状态：脱敏完成
     */
    private static final String PROCESS_MASK_DONE = "MASK_DONE";

    /**
     * 本地工作副本文件名
     */
    private static final String SOURCE_FILE_NAME = "original.docx";

    /**
     * 截取文件名
     */
    private static final String EXTRACT_FILE_NAME = "extract.docx";

    /**
     * 脱敏文件名
     */
    private static final String MASK_FILE_NAME = "masked.docx";

    /**
     * 脱敏文件在 sys_file 中的业务类型
     */
    private static final String BIZ_TYPE_MASK = "review_masked";

    /**
     * 脱敏文件批次前缀，配合 biz_id 关联到 review_doc.id
     */
    private static final String BATCH_MASK_PREFIX = "review_mask_";

    private final ReviewDocMapper docMapper;

    private final ReviewSpanMapper spanMapper;

    private final ISysFileService sysFileService;

    private final FileStorageStrategy storageStrategy;

    private final ReviewSensitiveRecognizer sensitiveRecognizer;

    /**
     * 中间产物工作目录，放在本地，不落在对外暴露的 profile 目录下
     */
    @Value("${ruoyi.review.work-dir}")
    private String workDir;

    /**
     * 章节命中关键词，多个用逗号分隔
     */
    @Value("${ruoyi.review.split-keywords:方案}")
    private String splitKeywords;

    @Override
    public TableDataInfo<ReviewDoc> pageDocs(ReviewDoc query, PageQuery pageQuery) {
        LambdaQueryWrapper<ReviewDoc> wrapper = Wrappers.<ReviewDoc>lambdaQuery()
            .like(StringUtils.isNotBlank(query.getProjectName()), ReviewDoc::getProjectName, query.getProjectName())
            .eq(StringUtils.isNotBlank(query.getProvinceCode()), ReviewDoc::getProvinceCode, query.getProvinceCode())
            .eq(StringUtils.isNotBlank(query.getProcessStatus()), ReviewDoc::getProcessStatus, query.getProcessStatus())
            .orderByDesc(ReviewDoc::getId);
        Page<ReviewDoc> page = docMapper.selectPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public ReviewDoc getDoc(Long docId) {
        return getDocOrThrow(docId);
    }

    @Override
    public void createDoc(ReviewDoc doc) {
        if (doc.getOriginalFileId() == null) {
            throw new ServiceException("请先上传投标文件");
        }
        validateBaseInfo(doc);
        SysFile sysFile = sysFileService.getById(doc.getOriginalFileId());
        if (sysFile == null) {
            throw new ServiceException("上传的文件不存在，请重新上传");
        }
        Date now = new Date();
        doc.setId(null);
        doc.setDocName(sysFile.getFileName());
        // 原始文件在 OSS，这里先记存储地址，拆分时会被替换为本地工作副本路径
        doc.setFilePath(sysFile.getOssUrl());
        doc.setStatus("0");
        doc.setExtractFilePath(null);
        doc.setMaskFilePath(null);
        doc.setMaskFileId(null);
        doc.setExtractChapters(null);
        doc.setProcessStatus(PROCESS_WAIT_SPLIT);
        doc.setProcessError(null);
        doc.setRetryCount(0);
        doc.setDelFlag("0");
        doc.setCreateTime(now);
        doc.setUpdateTime(now);
        docMapper.insert(doc);
    }

    @Override
    public void updateDoc(ReviewDoc doc) {
        if (doc.getId() == null) {
            throw new ServiceException("文档ID不能为空");
        }
        ReviewDoc db = getDocOrThrow(doc.getId());
        validateBaseInfo(doc);
        db.setProjectName(doc.getProjectName());
        db.setAmount(doc.getAmount());
        db.setProvinceCode(doc.getProvinceCode());
        db.setUpdateTime(new Date());
        docMapper.updateById(db);
    }

    @Override
    public void deleteDoc(Long docId) {
        getDocOrThrow(docId);
        docMapper.deleteById(docId);
    }

    @Override
    public ReviewDoc split(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        long start = System.currentTimeMillis();
        log.info("开始章节拆分 docId={} docName={} originalFileId={} keywords={}",
            docId, doc.getDocName(), doc.getOriginalFileId(), splitKeywords);
        if (doc.getOriginalFileId() == null) {
            throw new ServiceException("该文档没有原始文件，无法拆分章节");
        }
        File source = ensureLocalSource(doc);
        File output = new File(workDirOf(docId), EXTRACT_FILE_NAME);
        try {
            List<String> chapters = ReviewChapterSplitter.split(source.getAbsolutePath(), output.getAbsolutePath(), keywordList());
            String chapterText = String.join("；", chapters);
            // 重新拆分说明源文件变了，上一版脱敏文件（最终产物）作废，随后的重新脱敏会重新上传 OSS
            deleteMaskedFile(docId, doc.getMaskFileId());
            Date now = new Date();
            // 成功时 process_error 必须显式置空，updateById 会忽略 null 字段
            docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
                .eq(ReviewDoc::getId, docId)
                .set(ReviewDoc::getFilePath, source.getAbsolutePath())
                .set(ReviewDoc::getExtractFilePath, output.getAbsolutePath())
                .set(ReviewDoc::getExtractChapters, chapterText)
                .set(ReviewDoc::getMaskFileId, null)
                .set(ReviewDoc::getMaskFilePath, null)
                .set(ReviewDoc::getProcessStatus, PROCESS_SPLIT_DONE)
                .set(ReviewDoc::getProcessError, null)
                .set(ReviewDoc::getUpdateTime, now));
            doc.setFilePath(source.getAbsolutePath());
            doc.setExtractFilePath(output.getAbsolutePath());
            doc.setExtractChapters(chapterText);
            doc.setMaskFileId(null);
            doc.setMaskFilePath(null);
            doc.setProcessStatus(PROCESS_SPLIT_DONE);
            doc.setProcessError(null);
            doc.setUpdateTime(now);
            log.info("章节拆分完成 docId={} 命中章节数={} 章节={} 输出文件={} size={}B cost={}ms",
                docId, chapters.size(), chapterText, output.getAbsolutePath(), output.length(),
                System.currentTimeMillis() - start);
            return doc;
        } catch (ServiceException e) {
            log.warn("章节拆分未完成 docId={} keywords={} reason={}", docId, splitKeywords, e.getMessage());
            markSplitFailed(doc, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("章节拆分异常 docId={} docName={} cost={}ms", docId, doc.getDocName(),
                System.currentTimeMillis() - start, e);
            markSplitFailed(doc, e.getMessage());
            throw new ServiceException("章节拆分失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadExtract(Long docId, HttpServletResponse response) {
        ReviewDoc doc = getDocOrThrow(docId);
        if (StringUtils.isBlank(doc.getExtractFilePath())) {
            throw new ServiceException("尚未生成截取文件，请先执行章节拆分");
        }
        File file = new File(doc.getExtractFilePath());
        if (!file.exists()) {
            throw new ServiceException("截取文件不存在，请重新执行章节拆分");
        }
        writeLocalFile(docId, file, buildDownloadFileName(doc, "方案章节"), "截取文件", response);
    }

    @Override
    public void downloadMask(Long docId, HttpServletResponse response) {
        ReviewDoc doc = getDocOrThrow(docId);
        if (StringUtils.isBlank(doc.getMaskFilePath())) {
            throw new ServiceException("尚未生成脱敏文件，请先完成审核");
        }
        String fileName = buildDownloadFileName(doc, "脱敏");
        File local = new File(workDirOf(docId), MASK_FILE_NAME);
        if (local.exists() && local.length() > 0) {
            writeLocalFile(docId, local, fileName, "脱敏文件", response);
            return;
        }
        // 本地工作副本被清理时，从 OSS 回捞（脱敏文件是最终文件，已上传 OSS）
        log.info("本地脱敏副本不存在，改从 OSS 下载 docId={} ossUrl={}", docId, doc.getMaskFilePath());
        writeResponse(docId, fileName, "脱敏文件", null,
            () -> storageStrategy.getContent(doc.getMaskFilePath()), response);
    }

    private void writeLocalFile(Long docId, File file, String fileName, String label, HttpServletResponse response) {
        writeResponse(docId, fileName, label, file.length(), () -> Files.newInputStream(file.toPath()), response);
    }

    /**
     * 把文件流写回浏览器，下载来源可以是本地文件，也可以是 OSS
     */
    private void writeResponse(Long docId, String fileName, String label, Long size,
                               StreamSupplier streamSupplier, HttpServletResponse response) {
        long start = System.currentTimeMillis();
        log.info("开始下载{} docId={} fileName={} size={}B", label, docId, fileName, size);
        try {
            String asciiName = fileName.replaceAll("[^\\x20-\\x7E]", "_");
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedName);
            if (size != null) {
                response.setContentLengthLong(size);
            }
            try (InputStream in = streamSupplier.get();
                 OutputStream out = response.getOutputStream()) {
                IoUtil.copy(in, out);
                out.flush();
            }
            log.info("{}下载完成 docId={} fileName={} cost={}ms",
                label, docId, fileName, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("下载{}失败 docId={}", label, docId, e);
            throw new ServiceException("下载" + label + "失败: " + e.getMessage());
        }
    }

    @Override
    public ReviewRecognizeVO recognize(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        if (StringUtils.isBlank(doc.getExtractFilePath())) {
            throw new ServiceException("请先执行章节拆分，再执行脱敏");
        }
        if (!new File(doc.getExtractFilePath()).exists()) {
            throw new ServiceException("截取文件不存在，请重新执行章节拆分");
        }
        long start = System.currentTimeMillis();
        log.info("开始脱敏识别 docId={} docName={} 文件={}", docId, doc.getDocName(), doc.getExtractFilePath());
        ReviewRecognizeVO result = sensitiveRecognizer.recognize(doc);
        docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
            .eq(ReviewDoc::getId, docId)
            .set(ReviewDoc::getProcessStatus, PROCESS_WAIT_REVIEW)
            .set(ReviewDoc::getProcessError, null)
            .set(ReviewDoc::getUpdateTime, new Date()));
        log.info("脱敏识别结束 docId={} 流程状态={} 候选={} 新增={} cost={}ms",
            docId, PROCESS_WAIT_REVIEW, result.getCandidateCount(), result.getNewCount(),
            System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public ReviewContentVO content(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        checkFileExists(doc.getFilePath());
        try {
            List<ReviewNodeVO> nodes = ReviewDocxParser.parse(doc.getFilePath());
            List<ReviewSpan> spans = spanMapper.selectList(Wrappers.<ReviewSpan>lambdaQuery()
                .eq(ReviewSpan::getDocId, docId)
                .orderByAsc(ReviewSpan::getId));
            ReviewContentVO contentVO = new ReviewContentVO();
            contentVO.setDoc(doc);
            contentVO.setNodes(nodes);
            contentVO.setSpans(spans);
            return contentVO;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("解析文档失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean confirmSpan(Long id) {
        ReviewSpan span = getSpanOrThrow(id);
        span.setStatus("1");
        span.setUpdateTime(new Date());
        boolean updated = spanMapper.updateById(span) > 0;
        if (updated) {
            // 第一次确认后，文档从待审核进入审核中
            ReviewDoc doc = docMapper.selectById(span.getDocId());
            if (doc != null && "0".equals(doc.getStatus())) {
                doc.setStatus("1");
                doc.setUpdateTime(new Date());
                docMapper.updateById(doc);
            }
            invalidateMask(span.getDocId());
        }
        return updated;
    }

    @Override
    public Boolean ignoreSpan(Long id) {
        ReviewSpan span = getSpanOrThrow(id);
        span.setStatus("2");
        span.setUpdateTime(new Date());
        boolean updated = spanMapper.updateById(span) > 0;
        if (updated) {
            invalidateMask(span.getDocId());
        }
        return updated;
    }

    @Override
    public ReviewSpan addSpan(ReviewSpan span) {
        if (span.getDocId() == null) {
            throw new ServiceException("docId不能为空");
        }
        getDocOrThrow(span.getDocId());
        span.setSpanText(span.getSpanText().trim());
        validateWordSpan(span);
        checkUniqueSpan(span.getDocId(), null, span.getSpanText());
        Date now = new Date();
        span.setId(null);
        span.setBlockId(null);
        span.setStartChar(null);
        span.setEndChar(null);
        span.setSource(StringUtils.isBlank(span.getSource()) ? "A" : span.getSource());
        span.setStatus(StringUtils.isBlank(span.getStatus()) ? "0" : span.getStatus());
        span.setCreateTime(now);
        span.setUpdateTime(now);
        spanMapper.insert(span);
        invalidateMask(span.getDocId());
        return spanMapper.selectById(span.getId());
    }

    @Override
    public ReviewSpan updateSpan(ReviewSpan span) {
        ReviewSpan db = getSpanOrThrow(span.getId());
        span.setSpanText(span.getSpanText().trim());
        validateWordSpan(span);
        checkUniqueSpan(db.getDocId(), db.getId(), span.getSpanText());
        db.setSpanText(span.getSpanText());
        db.setSpanType(span.getSpanType());
        db.setUpdateTime(new Date());
        spanMapper.updateById(db);
        invalidateMask(db.getDocId());
        return spanMapper.selectById(db.getId());
    }

    @Override
    public Boolean completeReview(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        Long pendingCount = spanMapper.selectCount(Wrappers.<ReviewSpan>lambdaQuery()
            .eq(ReviewSpan::getDocId, docId)
            .eq(ReviewSpan::getStatus, "0"));
        if (pendingCount != null && pendingCount > 0) {
            throw new ServiceException("还有 " + pendingCount + " 条待确认记录未处理");
        }
        if ("2".equals(doc.getStatus()) && PROCESS_MASK_DONE.equals(doc.getProcessStatus())) {
            throw new ServiceException("该文档已完成审核和脱敏，如需重新生成脱敏文件请点击“重新脱敏”");
        }
        long start = System.currentTimeMillis();
        log.info("开始审核完成处理 docId={} docName={} 流程状态={}",
            docId, doc.getDocName(), doc.getProcessStatus());
        // 审核结果先落库：后面的脱敏失败不回滚审核状态，只写失败原因，等待重新脱敏
        Date now = new Date();
        docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
            .eq(ReviewDoc::getId, docId)
            .set(ReviewDoc::getStatus, "2")
            .set(ReviewDoc::getProcessStatus, PROCESS_WAIT_MASK)
            .set(ReviewDoc::getProcessError, null)
            .set(ReviewDoc::getUpdateTime, now));
        doc.setStatus("2");
        doc.setProcessStatus(PROCESS_WAIT_MASK);
        doc.setProcessError(null);
        doc.setUpdateTime(now);
        log.info("审核完成状态已保存 docId={} status=2 processStatus={} cost={}ms",
            docId, PROCESS_WAIT_MASK, System.currentTimeMillis() - start);
        // 审核完成即自动脱敏：用类型化掩码替换已确认的敏感词，生成往下走的脱敏文件
        autoMask(doc);
        return true;
    }

    @Override
    public String applyMask(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        long start = System.currentTimeMillis();
        log.info("开始重新脱敏 docId={} docName={} 源文件={}", docId, doc.getDocName(), doc.getExtractFilePath());
        int replaced = maskExtract(doc);
        log.info("重新脱敏完成 docId={} 替换处数={} maskFileId={} ossUrl={} cost={}ms",
            docId, replaced, doc.getMaskFileId(), doc.getMaskFilePath(),
            System.currentTimeMillis() - start);
        return doc.getMaskFilePath();
    }

    /**
     * 审核完成后的自动脱敏；脱敏失败只提示，不回滚审核状态，可从列表“重新脱敏”。
     */
    private void autoMask(ReviewDoc doc) {
        long start = System.currentTimeMillis();
        try {
            int replaced = maskExtract(doc);
            log.info("审核完成自动脱敏成功 docId={} 替换处数={} maskFileId={} ossUrl={} cost={}ms",
                doc.getId(), replaced, doc.getMaskFileId(), doc.getMaskFilePath(),
                System.currentTimeMillis() - start);
        } catch (ServiceException e) {
            log.warn("审核完成自动脱敏未成功 docId={} 流程状态={} reason={} cost={}ms",
                doc.getId(), PROCESS_WAIT_MASK, e.getMessage(), System.currentTimeMillis() - start);
            throw new ServiceException("审核已完成，但脱敏文件生成失败：" + e.getMessage()
                + "；可在列表中点击“重新脱敏”重试");
        }
    }

    /**
     * 对截取文件执行敏感词替换，结果写本地工作目录，只记路径不上传 OSS。
     * 失败时写 process_error 并把流程状态置为待脱敏，允许重试。
     *
     * @return 实际替换处数
     */
    private int maskExtract(ReviewDoc doc) {
        try {
            if (StringUtils.isBlank(doc.getExtractFilePath()) || !new File(doc.getExtractFilePath()).exists()) {
                throw new ServiceException("截取文件不存在，请重新执行章节拆分");
            }
            List<ReviewSpan> confirmed = spanMapper.selectList(Wrappers.<ReviewSpan>lambdaQuery()
                .eq(ReviewSpan::getDocId, doc.getId())
                .eq(ReviewSpan::getStatus, "1"));
            File output = new File(workDirOf(doc.getId()), MASK_FILE_NAME);
            int replaced = ReviewMaskApplier.apply(doc.getExtractFilePath(), confirmed, output.getAbsolutePath());
            if (replaced == 0) {
                // 全部忽略、或已确认的词在正文里找不到时，脱敏文件与截取文件内容一致
                log.warn("没有可替换的敏感词，脱敏文件与截取文件内容一致 docId={} 已确认词数={}",
                    doc.getId(), confirmed.size());
            }
            // 脱敏文件是流程的最终产物：按约定上传 OSS 并写 sys_file，字段里存 OSS 地址
            SysFile maskFile = uploadMaskedFile(output, buildDownloadFileName(doc, "脱敏"), doc);
            Date now = new Date();
            docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
                .eq(ReviewDoc::getId, doc.getId())
                .set(ReviewDoc::getMaskFileId, maskFile.getId())
                .set(ReviewDoc::getMaskFilePath, maskFile.getOssUrl())
                .set(ReviewDoc::getProcessStatus, PROCESS_MASK_DONE)
                .set(ReviewDoc::getProcessError, null)
                .set(ReviewDoc::getUpdateTime, now));
            doc.setMaskFileId(maskFile.getId());
            doc.setMaskFilePath(maskFile.getOssUrl());
            doc.setProcessStatus(PROCESS_MASK_DONE);
            doc.setProcessError(null);
            doc.setUpdateTime(now);
            return replaced;
        } catch (ServiceException e) {
            markMaskFailed(doc, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("脱敏替换异常 docId={} docName={}", doc.getId(), doc.getDocName(), e);
            markMaskFailed(doc, e.getMessage());
            throw new ServiceException("脱敏处理失败: " + e.getMessage());
        }
    }

    /**
     * 上传脱敏文件到 OSS 并写 sys_file 记录；新文件传成功后再删上一版，避免上传失败时旧文件也没了。
     */
    private SysFile uploadMaskedFile(File masked, String fileName, ReviewDoc doc) {
        long start = System.currentTimeMillis();
        String batchId = BATCH_MASK_PREFIX + doc.getId();
        log.info("开始上传脱敏文件 docId={} fileName={} size={}B bizType={}",
            doc.getId(), fileName, masked.length(), BIZ_TYPE_MASK);
        SysFile sysFile = sysFileService.upload(new LocalFileMultipartFile(masked, fileName), BIZ_TYPE_MASK, batchId);
        sysFileService.updateBizId(batchId, doc.getId());
        log.info("脱敏文件上传完成 docId={} maskFileId={} ossUrl={} size={}B cost={}ms",
            doc.getId(), sysFile.getId(), sysFile.getOssUrl(), masked.length(),
            System.currentTimeMillis() - start);
        deleteMaskedFile(doc.getId(), doc.getMaskFileId());
        return sysFile;
    }

    /**
     * 删除上一版脱敏文件（OSS 对象 + sys_file 记录），删除失败不影响主流程
     */
    private void deleteMaskedFile(Long docId, Long maskFileId) {
        if (maskFileId == null) {
            return;
        }
        try {
            sysFileService.deleteById(maskFileId);
            log.info("已删除上一版脱敏文件 docId={} maskFileId={}", docId, maskFileId);
        } catch (Exception e) {
            log.warn("删除上一版脱敏文件失败 docId={} maskFileId={} reason={}",
                docId, maskFileId, e.getMessage());
        }
    }

    private void markMaskFailed(ReviewDoc doc, String message) {
        String error = StringUtils.substring(message, 0, 500);
        docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
            .eq(ReviewDoc::getId, doc.getId())
            .set(ReviewDoc::getProcessStatus, PROCESS_WAIT_MASK)
            .set(ReviewDoc::getProcessError, error)
            .set(ReviewDoc::getRetryCount, (doc.getRetryCount() == null ? 0 : doc.getRetryCount()) + 1)
            .set(ReviewDoc::getUpdateTime, new Date()));
        doc.setProcessStatus(PROCESS_WAIT_MASK);
        doc.setProcessError(error);
    }

    /**
     * 审核完成之后又改动了敏感词，之前生成的脱敏文件就过期了：
     * 清掉路径回到“待脱敏”，可以在列表点击“重新脱敏”按最新词表重新生成。
     */
    private void invalidateMask(Long docId) {
        ReviewDoc doc = docMapper.selectById(docId);
        if (doc == null || !"2".equals(doc.getStatus()) || StringUtils.isBlank(doc.getMaskFilePath())) {
            return;
        }
        log.info("审核完成后敏感词发生变化，脱敏文件失效 docId={} maskFileId={} ossUrl={}",
            docId, doc.getMaskFileId(), doc.getMaskFilePath());
        deleteMaskedFile(docId, doc.getMaskFileId());
        docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
            .eq(ReviewDoc::getId, docId)
            .set(ReviewDoc::getMaskFileId, null)
            .set(ReviewDoc::getMaskFilePath, null)
            .set(ReviewDoc::getProcessStatus, PROCESS_WAIT_MASK)
            .set(ReviewDoc::getProcessError, null)
            .set(ReviewDoc::getUpdateTime, new Date()));
    }

    /**
     * 下载流来源（本地文件或 OSS），允许抛受检异常
     */
    @FunctionalInterface
    private interface StreamSupplier {

        InputStream get() throws Exception;
    }

    private ReviewDoc getDocOrThrow(Long docId) {
        ReviewDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new ServiceException("文档不存在");
        }
        return doc;
    }

    private ReviewSpan getSpanOrThrow(Long id) {
        ReviewSpan span = spanMapper.selectById(id);
        if (span == null) {
            throw new ServiceException("识别记录不存在");
        }
        return span;
    }

    private void validateWordSpan(ReviewSpan span) {
        if (StringUtils.isBlank(span.getSpanText())) {
            throw new ServiceException("敏感词不能为空");
        }
        if (StringUtils.isBlank(span.getSpanType())) {
            throw new ServiceException("敏感类型不能为空");
        }
    }

    private void checkUniqueSpan(Long docId, Long excludeId, String spanText) {
        Long count = spanMapper.selectCount(Wrappers.<ReviewSpan>lambdaQuery()
            .eq(ReviewSpan::getDocId, docId)
            .eq(ReviewSpan::getSpanText, spanText)
            .ne(excludeId != null, ReviewSpan::getId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException("该敏感词在当前文档中已存在");
        }
    }

    private void checkFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ServiceException("文档文件不存在: " + filePath);
        }
    }

    private void validateBaseInfo(ReviewDoc doc) {
        if (StringUtils.isBlank(doc.getProjectName())) {
            throw new ServiceException("项目名称不能为空");
        }
        if (doc.getAmount() == null) {
            doc.setAmount(BigDecimal.ZERO);
        }
        if (doc.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("项目金额不能为负数");
        }
        if (StringUtils.isBlank(doc.getProvinceCode()) || !doc.getProvinceCode().matches("\\d{6}")) {
            throw new ServiceException("请选择正确的省份");
        }
    }

    /**
     * 确保本地存在原始文件副本，不存在时从 OSS 拉取。
     */
    private File ensureLocalSource(ReviewDoc doc) {
        File dir = workDirOf(doc.getId());
        File local = new File(dir, SOURCE_FILE_NAME);
        if (local.exists() && local.length() > 0) {
            log.info("复用本地工作副本 docId={} path={} size={}B",
                doc.getId(), local.getAbsolutePath(), local.length());
            return local;
        }
        SysFile sysFile = sysFileService.getById(doc.getOriginalFileId());
        if (sysFile == null || StringUtils.isBlank(sysFile.getOssUrl())) {
            throw new ServiceException("原始文件不存在，请重新上传");
        }
        mkdirs(dir);
        long start = System.currentTimeMillis();
        try (InputStream in = storageStrategy.getContent(sysFile.getOssUrl());
             OutputStream out = Files.newOutputStream(local.toPath())) {
            IoUtil.copy(in, out);
        } catch (Exception e) {
            log.error("下载原始文件失败 docId={} ossUrl={}", doc.getId(), sysFile.getOssUrl(), e);
            throw new ServiceException("下载原始文件失败: " + e.getMessage());
        }
        log.info("原始文件下载完成 docId={} ossUrl={} size={}B cost={}ms",
            doc.getId(), sysFile.getOssUrl(), local.length(), System.currentTimeMillis() - start);
        return local;
    }

    private void markSplitFailed(ReviewDoc doc, String message) {
        doc.setProcessError(StringUtils.substring(message, 0, 500));
        doc.setRetryCount((doc.getRetryCount() == null ? 0 : doc.getRetryCount()) + 1);
        doc.setUpdateTime(new Date());
        docMapper.updateById(doc);
    }

    private File workDirOf(Long docId) {
        File dir = new File(workDir, String.valueOf(docId));
        mkdirs(dir);
        return dir;
    }

    private void mkdirs(File dir) {
        if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory()) {
            throw new ServiceException("创建本地工作目录失败: " + dir.getAbsolutePath());
        }
    }

    private String buildDownloadFileName(ReviewDoc doc, String suffix) {
        String name = StringUtils.isBlank(doc.getDocName()) ? "投标文件" : doc.getDocName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name + "-" + suffix + ".docx";
    }

    private List<String> keywordList() {
        List<String> keywords = new ArrayList<>();
        String source = StringUtils.isBlank(splitKeywords) ? "方案" : splitKeywords;
        for (String keyword : source.split(",")) {
            String trimmed = keyword.trim();
            if (StringUtils.isNotBlank(trimmed)) {
                keywords.add(trimmed);
            }
        }
        return keywords;
    }

}
