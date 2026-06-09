import request from '@/utils/request'

export function listInspection(query) {
  return request({ url: '/lims/inspection/list', method: 'get', params: query })
}
export function getInspection(id) {
  return request({ url: '/lims/inspection/' + id, method: 'get' })
}
export function addInspection(data) {
  return request({ url: '/lims/inspection', method: 'post', data: data })
}
export function updateInspection(data) {
  return request({ url: '/lims/inspection', method: 'put', data: data })
}
export function delInspection(ids) {
  return request({ url: '/lims/inspection/' + ids, method: 'delete' })
}

export function listTestItem(query) {
  return request({ url: '/lims/testItem/list', method: 'get', params: query })
}
export function updateTestItem(data) {
  return request({ url: '/lims/testItem', method: 'put', data: data })
}

// 获取当前任务的检测项目（多实例）
export function getTestItemByTask(taskId) {
  return request({ url: '/lims/testItem/getByTask/' + taskId, method: 'get' })
}

// 提交报检并启动流程
export function submitInspection(data) {
  return request({ url: '/lims/inspection/submit', method: 'post', data: data })
}
