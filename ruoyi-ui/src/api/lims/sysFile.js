import request from '@/utils/request'

export function uploadFile(data) { return request({ url: '/common/file/upload', method: 'post', data: data }) }
export function deleteFile(id) { return request({ url: '/common/file/' + id, method: 'delete' }) }
export function listByBatch(batchId) { return request({ url: '/common/file/listByBatch/' + batchId, method: 'get' }) }
export function getSignUrl(fileId) { return request({ url: '/common/file/signUrl/' + fileId, method: 'get' }) }
export function getEditorConfig(fileId) { return request({ url: '/common/file/editorConfig/' + fileId, method: 'get' }) }
