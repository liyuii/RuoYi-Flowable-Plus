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

// 按已确认记录执行脱敏
export function applyReviewDoc(docId) {
  return request({
    url: '/demo/review/doc/' + docId + '/apply',
    method: 'post'
  })
}
