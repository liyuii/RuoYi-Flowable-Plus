import request from '@/utils/request'

export function listTestDemo(query) {
  return request({ url: '/lims/testDemo/list', method: 'get', params: query })
}
export function getTestDemo(id) {
  return request({ url: '/lims/testDemo/' + id, method: 'get' })
}
export function addTestDemo(data) {
  return request({ url: '/lims/testDemo', method: 'post', data: data })
}
export function updateTestDemo(data) {
  return request({ url: '/lims/testDemo', method: 'put', data: data })
}
export function delTestDemo(ids) {
  return request({ url: '/lims/testDemo/' + ids, method: 'delete' })
}