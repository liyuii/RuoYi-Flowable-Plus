import request from '@/utils/request'

// 抽取申请列表
export function listExtract(query) {
  return request({ url: '/lims/expertExtract/list', method: 'get', params: query })
}

// 抽取申请详情，带范围、回避名单、结果
export function getExtract(id) {
  return request({ url: '/lims/expertExtract/' + id, method: 'get' })
}

// 新增申请
export function addExtract(data) {
  return request({ url: '/lims/expertExtract', method: 'post', data: data })
}

// 修改申请
export function updateExtract(data) {
  return request({ url: '/lims/expertExtract', method: 'put', data: data })
}

// 删除申请
export function delExtract(ids) {
  return request({ url: '/lims/expertExtract/' + ids, method: 'delete' })
}

// 提交审核
export function submitExtract(id) {
  return request({ url: '/lims/expertExtract/submit/' + id, method: 'post' })
}

// 审核
export function auditExtract(data) {
  return request({ url: '/lims/expertExtract/audit', method: 'post', data: data })
}

// 预检：只看每条范围够不够，不落库
export function checkExtract(id) {
  return request({ url: '/lims/expertExtract/check/' + id, method: 'get' })
}

// 统计各专业当前可抽取的人数，给选择专业的下拉用
export function majorCount(data) {
  return request({ url: '/lims/expertExtract/majorCount', method: 'post', data: data })
}

// 首次抽取
export function drawExtract(id) {
  return request({ url: '/lims/expertExtract/draw/' + id, method: 'post' })
}

// 补充抽取
export function supplementExtract(id) {
  return request({ url: '/lims/expertExtract/supplement/' + id, method: 'post' })
}

// 变更某位专家的参加状态
export function updateResultStatus(data) {
  return request({ url: '/lims/expertExtract/result/status', method: 'post', data: data })
}
