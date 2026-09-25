package com.ruoyi.doc.service;

import com.ruoyi.doc.domain.dto.DocSyncDTO;

/**
 * 文档同步服务：接收源系统同步过来的文档（本地模拟调用，将来换成 HTTP 接口）
 */
public interface IDocSyncService {

    /**
     * 同步一份文档（幂等：按 sourceType + sourceBizId 新增或更新）
     *
     * @return 文档库主键 doc_info.id
     */
    Long sync(DocSyncDTO dto);

}
