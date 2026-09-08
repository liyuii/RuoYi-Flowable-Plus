package com.ruoyi.demo.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.demo.config.DemoUploadProperties;
import com.ruoyi.demo.domain.DemoUploadChunk;
import com.ruoyi.demo.domain.DemoUploadFile;
import com.ruoyi.demo.domain.bo.DemoUploadCompleteBo;
import com.ruoyi.demo.domain.bo.DemoUploadInitBo;
import com.ruoyi.demo.domain.vo.DemoUploadSessionVo;
import com.ruoyi.demo.mapper.DemoUploadChunkMapper;
import com.ruoyi.demo.mapper.DemoUploadFileMapper;
import com.ruoyi.demo.service.IDemoUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private final DemoUploadFileMapper fileMapper;

    private final DemoUploadChunkMapper chunkMapper;

    private final DemoUploadProperties uploadProperties;

    @Override
    public DemoUploadSessionVo initUpload(DemoUploadInitBo bo) {
        if (bo.getFileSize() == null || bo.getFileSize() <= 0) {
            throw new ServiceException("文件大小必须大于 0");
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
        Date now = new Date();
        DemoUploadFile upload = new DemoUploadFile();
        upload.setUploadId(uploadId);
        upload.setFileName(sanitizeFileName(bo.getFileName()));
        upload.setFileSize(bo.getFileSize());
        upload.setFileHash(bo.getFileHash());
        upload.setChunkSize((int) chunkSize);
        upload.setTotalChunks(totalChunks);
        upload.setStatus(STATUS_UPLOADING);
        upload.setDeleted(0);
        upload.setCreateTime(now);
        upload.setUpdateTime(now);
        fileMapper.insert(upload);

        DemoUploadSessionVo vo = new DemoUploadSessionVo();
        vo.setUploadId(uploadId);
        vo.setChunkSize(upload.getChunkSize());
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
    public TableDataInfo<DemoUploadFile> pageUploadFiles(PageQuery pageQuery) {
        LambdaQueryWrapper<DemoUploadFile> wrapper = Wrappers.<DemoUploadFile>lambdaQuery()
            .eq(DemoUploadFile::getStatus, STATUS_COMPLETE)
            .eq(DemoUploadFile::getDeleted, 0)
            .orderByDesc(DemoUploadFile::getId);
        Page<DemoUploadFile> page = fileMapper.selectPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public void downloadFile(Long id, HttpServletResponse response) {
        DemoUploadFile upload = fileMapper.selectOne(Wrappers.<DemoUploadFile>lambdaQuery()
            .eq(DemoUploadFile::getId, id)
            .eq(DemoUploadFile::getStatus, STATUS_COMPLETE)
            .eq(DemoUploadFile::getDeleted, 0));
        if (upload == null || StringUtils.isBlank(upload.getStoragePath())) {
            throw new ServiceException("文件不存在或已删除");
        }
        Path path = new File(upload.getStoragePath()).toPath();
        if (!Files.exists(path)) {
            throw new ServiceException("文件已从磁盘丢失");
        }
        try {
            String fileName = upload.getFileName();
            String asciiName = fileName.replaceAll("[^\\x20-\\x7E]", "_");
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encodedName);
            response.setContentLengthLong(Files.size(path));
            try (InputStream in = Files.newInputStream(path);
                 OutputStream out = response.getOutputStream()) {
                copyStream(in, out);
                out.flush();
            }
        } catch (Exception e) {
            log.error("下载文件失败 id={}", id, e);
            throw new ServiceException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(Long id) {
        DemoUploadFile upload = fileMapper.selectById(id);
        if (upload == null || Integer.valueOf(1).equals(upload.getDeleted())) {
            throw new ServiceException("文件不存在或已删除");
        }
        DemoUploadFile update = new DemoUploadFile();
        update.setId(id);
        update.setDeleted(1);
        update.setUpdateTime(new Date());
        fileMapper.updateById(update);
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
