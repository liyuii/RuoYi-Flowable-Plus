import request from '@/utils/request'

export function listTestGroup(query) {
  return request({ url: '/lims/testGroup/list', method: 'get', params: query })
}
export function getTestGroup(id) {
  return request({ url: '/lims/testGroup/' + id, method: 'get' })
}
export function addTestGroup(data) {
  return request({ url: '/lims/testGroup', method: 'post', data: data })
}
export function updateTestGroup(data) {
  return request({ url: '/lims/testGroup', method: 'put', data: data })
}
export function delTestGroup(ids) {
  return request({ url: '/lims/testGroup/' + ids, method: 'delete' })
}

export function listGroupMember(query) {
  return request({ url: '/lims/testGroupMember/list', method: 'get', params: query })
}
export function addGroupMember(data) {
  return request({ url: '/lims/testGroupMember', method: 'post', data: data })
}
export function delGroupMember(ids) {
  return request({ url: '/lims/testGroupMember/' + ids, method: 'delete' })
}
