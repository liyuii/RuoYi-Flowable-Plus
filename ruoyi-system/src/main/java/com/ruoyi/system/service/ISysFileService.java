package com.ruoyi.system.service;

import com.ruoyi.system.domain.SysFile;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ISysFileService {
    SysFile upload(MultipartFile file, String bizType, String batchId);
   Boolean deleteById(Long id);
   List<SysFile> listByBatch(String batchId);
    void updateBizId(String batchId, Long bizId);
}
