import request from '@/utils/request'

// 专家库列表
export function listExpert(query) {
  return request({ url: '/lims/expertInfo/list', method: 'get', params: query })
}

// 专家详情
export function getExpert(id) {
  return request({ url: '/lims/expertInfo/' + id, method: 'get' })
}

// 新增专家
export function addExpert(data) {
  return request({ url: '/lims/expertInfo', method: 'post', data: data })
}

// 修改专家
export function updateExpert(data) {
  return request({ url: '/lims/expertInfo', method: 'put', data: data })
}

// 删除专家
export function delExpert(ids) {
  return request({ url: '/lims/expertInfo/' + ids, method: 'delete' })
}
