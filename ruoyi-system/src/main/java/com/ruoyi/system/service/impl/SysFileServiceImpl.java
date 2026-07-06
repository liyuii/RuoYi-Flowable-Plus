package com.ruoyi.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.mapper.SysFileMapper;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysFileServiceImpl implements ISysFileService {

    private final SysFileMapper baseMapper;

   @Value("${ruoyi.profile}")
   private String profile;

   @Override
   public SysFile upload(MultipartFile file, String bizType, String batchId) {
       if (ObjectUtil.isNull(file)) {
           throw new ServiceException("上传文件不能为空");
       }
       String originalName = file.getOriginalFilename();
       String suffix = StringUtils.substring(originalName, originalName.lastIndexOf("."), originalName.length());
       try {
            LocalDate today = LocalDate.now();
            String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String day = String.format("%02d", today.getDayOfMonth());
            String uploadDir = profile + File.separator + bizType + File.separator + yearMonth + File.separator + day;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
           long size = file.getSize();
           String newFileName = UUID.randomUUID().toString() + suffix;
            File dest = new File(dir, newFileName);
            file.transferTo(dest);
            SysFile sysFile = new SysFile();
            sysFile.setBizType(bizType);
            sysFile.setBatchId(batchId);
            sysFile.setFileName(originalName);
            sysFile.setFileSuffix(suffix);
            sysFile.setFileSize(size);
            sysFile.setOssUrl("/profile/" + bizType + "/" + yearMonth + "/" + day + "/" + newFileName);
            sysFile.setCreateBy(LoginHelper.getUsername());
            sysFile.setCreateTime(new Date());
           baseMapper.insert(sysFile);
           return sysFile;
       } catch (IOException e) {
            throw new ServiceException("文件保存失败: " + e.getMessage());
       }
   }

    @Override
    public Boolean deleteById(Long id) {
        SysFile sysFile = baseMapper.selectById(id);
        if (ObjectUtil.isNull(sysFile)) {
            return false;
        }
        try {
            String relativePath = sysFile.getOssUrl().replace("/profile/", "");
            String fullPath = profile + File.separator + relativePath;
            File file = new File(fullPath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            log.warn("删除本地文件失败: {}", e.getMessage());
        }
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public void updateBizId(String batchId, Long bizId) {
        SysFile update = new SysFile();
        update.setBizId(bizId);
        baseMapper.update(update, Wrappers.<SysFile>lambdaUpdate().eq(SysFile::getBatchId, batchId));
    }

    @Override
    public List<SysFile> listByBatch(String batchId) {
        LambdaQueryWrapper<SysFile> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysFile::getBatchId, batchId);
        lqw.orderByAsc(SysFile::getId);
        return baseMapper.selectList(lqw);
    }
}
