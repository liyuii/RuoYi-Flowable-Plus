import request from '@/utils/request'

export function listSpecification(query) {
  return request({ url: '/lims/specification/list', method: 'get', params: query })
}
export function getSpecification(id) {
  return request({ url: '/lims/specification/' + id, method: 'get' })
}
export function addSpecification(data) {
  return request({ url: '/lims/specification', method: 'post', data: data })
}
export function updateSpecification(data) {
  return request({ url: '/lims/specification', method: 'put', data: data })
}
export function delSpecification(ids) {
  return request({ url: '/lims/specification/' + ids, method: 'delete' })
}

export function listSpecItem(query) {
  return request({ url: '/lims/specItem/list', method: 'get', params: query })
}
export function addSpecItem(data) {
  return request({ url: '/lims/specItem', method: 'post', data: data })
}
export function updateSpecItem(data) {
  return request({ url: '/lims/specItem', method: 'put', data: data })
}
export function delSpecItem(ids) {
  return request({ url: '/lims/specItem/' + ids, method: 'delete' })
}
