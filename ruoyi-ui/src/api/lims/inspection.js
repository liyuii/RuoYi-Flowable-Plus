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
