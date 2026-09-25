package com.ruoyi.system.service;

import com.ruoyi.system.domain.SysFile;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ISysFileService {
   SysFile upload(MultipartFile file, String bizType, String batchId);

   /**
    * 上传文件并显式指定创建人
    * <p>异步线程（@Async）里没有登录上下文，LoginHelper 取不到用户，
    * 这种场景用本方法传 createBy（系统生成的文件传 "system"）。
    */
   SysFile upload(MultipartFile file, String bizType, String batchId, String createBy);

   Boolean deleteById(Long id);

   List<SysFile> listByBatch(String batchId);

   void updateBizId(String batchId, Long bizId);

   SysFile getById(Long id);

   void overwriteFile(Long id, byte[] content);
}
