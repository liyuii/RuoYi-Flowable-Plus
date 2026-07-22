package com.ruoyi.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.mapper.SysFileMapper;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import com.ruoyi.oss.service.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysFileServiceImpl implements ISysFileService {

    private final SysFileMapper baseMapper;

    private final FileStorageStrategy storageStrategy;
   @Override
   public SysFile upload(MultipartFile file, String bizType, String batchId) {
        // 在调用存储策略之前读取文件信息（上传后 MultipartFile 的临时文件可能被清理）
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        long fileSize = file.getSize();

        String fileUrl = storageStrategy.upload(file, bizType);
        SysFile sysFile = new SysFile();
        sysFile.setBizType(bizType);
        sysFile.setBatchId(batchId);
        sysFile.setFileName(originalName);
        sysFile.setFileSuffix(suffix);
        sysFile.setFileSize(fileSize);
        sysFile.setOssUrl(fileUrl);
        sysFile.setCreateBy(LoginHelper.getUsername());
        sysFile.setCreateTime(new Date());
        baseMapper.insert(sysFile);
        return sysFile;
   }

    @Override
    public Boolean deleteById(Long id) {
        SysFile sysFile = baseMapper.selectById(id);
        if (ObjectUtil.isNull(sysFile)) {
            return false;
        }
        storageStrategy.delete(sysFile.getOssUrl());
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public void updateBizId(String batchId, Long bizId) {
        SysFile update = new SysFile();
        update.setBizId(bizId);
        baseMapper.update(update, Wrappers.<SysFile>lambdaUpdate().eq(SysFile::getBatchId, batchId));
    }

    @Override
    public SysFile getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<SysFile> listByBatch(String batchId) {
        LambdaQueryWrapper<SysFile> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysFile::getBatchId, batchId);
        lqw.orderByAsc(SysFile::getId);
        return baseMapper.selectList(lqw);
    }

    @Override
    public void overwriteFile(Long id, byte[] content) {
        SysFile sysFile = baseMapper.selectById(id);
        if (sysFile == null) {
            throw new ServiceException("文件不存在");
        }
        storageStrategy.overwrite(sysFile.getOssUrl(), content);
    }
}
