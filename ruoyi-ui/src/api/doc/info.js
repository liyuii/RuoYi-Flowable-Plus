import request from '@/utils/request'

// 查询文档列表
export function listDocInfo(query) {
  return request({
    url: '/doc/info/list',
    method: 'get',
    params: query
  })
}

// 查询文档详情（文档信息 + 页面图片）
export function getDocInfo(id) {
  return request({
    url: '/doc/info/' + id,
    method: 'get'
  })
}

// 删除文档
export function delDocInfo(id) {
  return request({
    url: '/doc/info/' + id,
    method: 'delete'
  })
}

// 下载文档（整个 docx）
export function downloadDocInfo(id) {
  return request({
    url: '/doc/info/' + id + '/download',
    method: 'get',
    responseType: 'blob'
  })
}
