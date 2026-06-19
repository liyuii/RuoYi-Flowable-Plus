import request from '@/utils/request'
export function listContract(query) { return request({ url: '/lims/contract/list', method: 'get', params: query }) }
export function getContract(id) { return request({ url: '/lims/contract/' + id, method: 'get' }) }
export function addContract(data) { return request({ url: '/lims/contract', method: 'post', data: data }) }
export function updateContract(data) { return request({ url: '/lims/contract', method: 'put', data: data }) }
export function delContract(ids) { return request({ url: '/lims/contract/' + ids, method: 'delete' }) }
export function submitContract(data) { return request({ url: '/lims/contract/submit', method: 'post', data: data }) }