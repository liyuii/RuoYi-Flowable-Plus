import request from '@/utils/request'

// 查询人工审核文档列表
export function listReviewDoc(query) {
  return request({
    url: '/demo/review/doc/list',
    method: 'get',
    params: query
  })
}

// 查询审核页数据（文本块 + 高亮记录）
export function getReviewContent(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/content',
    method: 'get'
  })
}

// 确认识别记录
export function confirmReviewSpan(id) {
  return request({
    url: '/demo/review/span/' + id + '/confirm',
    method: 'put'
  })
}

// 忽略识别记录
export function ignoreReviewSpan(id) {
  return request({
    url: '/demo/review/span/' + id + '/ignore',
    method: 'put'
  })
}

// 手动补充识别记录
export function addReviewSpan(data) {
  return request({
    url: '/demo/review/span',
    method: 'post',
    data: data
  })
}

// 调整识别记录范围
export function updateReviewSpan(data) {
  return request({
    url: '/demo/review/span',
    method: 'put',
    data: data
  })
}

// 审核完成（校验无待确认记录后，文档状态改为 2）
export function completeReviewDoc(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/complete',
    method: 'post'
  })
}

// 查询投标文件详情
export function getReviewDoc(docId) {
  return request({
    url: '/demo/review/doc/' + docId,
    method: 'get'
  })
}

// 新增投标文件（原文件需先上传到 OSS，传 originalFileId）
export function addReviewDoc(data) {
  return request({
    url: '/demo/review/doc',
    method: 'post',
    data: data
  })
}

// 修改投标文件基础信息
export function updateReviewDoc(data) {
  return request({
    url: '/demo/review/doc',
    method: 'put',
    data: data
  })
}

// 逻辑删除投标文件
export function delReviewDoc(docId) {
  return request({
    url: '/demo/review/doc/' + docId,
    method: 'delete'
  })
}

// 拆分方案章节
export function splitReviewDoc(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/split',
    method: 'post',
    timeout: 180000
  })
}

// 下载截取文件
export function downloadReviewExtract(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/extract/download',
    method: 'get',
    responseType: 'blob'
  })
}

// 删除已上传的文件（同时删除 sys_file 记录与 OSS 对象）
export function delReviewFile(fileId) {
  return request({
    url: '/common/file/' + fileId,
    method: 'delete'
  })
}

// 脱敏识别：对截取文件执行敏感词识别（正则 + AI），候选词进入人工审核
export function recognizeReviewDoc(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/recognize',
    method: 'post',
    timeout: 180000,
    headers: { repeatSubmit: false }
  })
}

// 重新脱敏：按最新词表重新生成脱敏文件（审核完成时自动脱敏失败后重试）
export function applyReviewMask(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/apply',
    method: 'post',
    timeout: 180000,
    headers: { repeatSubmit: false }
  })
}

// 下载脱敏文件（审核完成时自动生成在本地工作目录）
export function downloadReviewMask(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/mask/download',
    method: 'get',
    responseType: 'blob'
  })
}
