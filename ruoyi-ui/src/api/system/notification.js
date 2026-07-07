import request from '@/utils/request'

export function listNotification(query) {
  return request({ url: '/system/notification/list', method: 'get', params: query })
}

export function unreadCount() {
  return request({ url: '/system/notification/unreadCount', method: 'get' })
}

export function markRead(id) {
  return request({ url: '/system/notification/read/' + id, method: 'put' })
}

export function markAllRead() {
  return request({ url: '/system/notification/readAll', method: 'put' })
}