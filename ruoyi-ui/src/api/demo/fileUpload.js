import request from '@/utils/request'

// 初始化上传
export function initUpload(data) {
  return request({
    url: '/demo/upload/init',
    method: 'post',
    data: data,
    headers: { repeatSubmit: false }
  })
}

// 查询未完成上传进度
export function getUploadProgress(fileHash) {
  return request({
    url: '/demo/upload/progress',
    method: 'get',
    params: { fileHash: fileHash }
  })
}

// 上传单个分片
export function uploadChunk(data) {
  return request({
    url: '/demo/upload/chunk',
    method: 'post',
    data: data,
    timeout: 120000,
    headers: { repeatSubmit: false }
  })
}

// 合并文件
export function completeUpload(data) {
  return request({
    url: '/demo/upload/complete',
    method: 'post',
    data: data,
    headers: { repeatSubmit: false }
  })
}

// 取消上传
export function cancelUpload(uploadId) {
  return request({
    url: '/demo/upload/cancel?uploadId=' + uploadId,
    method: 'post',
    headers: { repeatSubmit: false }
  })
}

// 查询已完成文件列表
export function listUploadFiles(query) {
  return request({
    url: '/demo/upload/list',
    method: 'get',
    params: query
  })
}

// 下载文件
export function downloadUploadFile(id) {
  return request({
    url: '/demo/upload/download/' + id,
    method: 'get',
    responseType: 'blob'
  })
}

// 逻辑删除文件
export function deleteUploadFile(id) {
  return request({
    url: '/demo/upload/file/' + id,
    method: 'delete'
  })
}
