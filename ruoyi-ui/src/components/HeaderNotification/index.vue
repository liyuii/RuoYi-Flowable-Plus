<template>
    <el-dropdown trigger="click" placement="bottom" @command="handleCommand" @visible-change="handleDropdown">
    <el-badge :value="unread" :hidden="unread === 0" class="notification-badge">
      <i class="el-icon-bell" :class="{ 'has-unread': unread > 0 }" />
    </el-badge>
    <el-dropdown-menu slot="dropdown" class="notification-dropdown">
      <!-- 标题 -->
      <div class="notif-header">
        <span class="notif-header-title">消息通知</span>
        <span class="notif-header-count" v-if="unread > 0">{{ unread }} 条未读</span>
      </div>

      <!-- 列表 -->
      <div class="notif-body" v-if="list.length > 0">
        <div
          v-for="item in list" :key="item.id"
          class="notif-item"
          :class="{ 'notif-unread': item.isRead === '0' }"
          @click="handleClickItem(item)"
        >
          <div class="notif-dot" v-if="item.isRead === '0'"></div>
          <div class="notif-content-wrap">
            <div class="notif-title-row">
              <span class="notif-title">{{ item.title }}</span>
              <span class="notif-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="notif-desc">{{ item.content }}</div>
          </div>
        </div>
      </div>
      <div v-else class="notif-empty">暂无通知</div>

      <!-- 底部操作 -->
      <div class="notif-footer">
        <el-button type="text" @click="goTodoList">查看全部</el-button>
        <el-button type="text" @click="markAllRead" v-if="unread > 0">全部已读</el-button>
      </div>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import { listNotification, unreadCount, markRead, markAllRead } from '@/api/system/notification'
import { getTaskFormKey } from '@/api/workflow/task'

export default {
  name: 'HeaderNotification',
  data() {
    return {
      unread: 0,
      list: [],
      polling: null
    }
  },
  mounted() {
    this.fetchData()
    this.polling = setInterval(() => this.fetchUnreadCount(), 30000)
  },
  beforeDestroy() {
    clearInterval(this.polling)
  },
  methods: {
    fetchData() {
      unreadCount().then(r => { this.unread = r.data || 0 })
      listNotification().then(r => { this.list = r.data || [] })
    },
    fetchUnreadCount() {
      unreadCount().then(r => { this.unread = r.data || 0 })
    },
    handleDropdown(visible) {
      if (visible) this.fetchData()
    },
    handleClickItem(item) {
      markRead(item.id)
      item.isRead = '1'
      getTaskFormKey(item.taskId).then(res => {
        const formKey = res?.msg || res || ''
        if (!formKey) { this.$router.push('/workflow/work/todo'); return }
        const url = formKey
          .replace(/{taskId}/g, item.taskId || '')
          .replace(/{businessKey}/g, item.businessKey || '')
          .replace(/{processInstanceId}/g, item.procInsId || '')
          .replace(/\${procInsId}/g, item.procInsId || '')
        this.$router.push(url)
        this.$emit('close')
      })
    },
    markAllRead() {
      markAllRead().then(() => {
        this.unread = 0
        this.list.forEach(n => n.isRead = '1')
      })
    },
   goTodoList() {
      this.$router.push('/work/todo')
   },
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      const now = new Date()
      const diff = (now - d) / 1000
      if (diff < 60) return '刚刚'
      if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
      if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
      const m = (d.getMonth() + 1).toString().padStart(2, '0')
      const day = d.getDate().toString().padStart(2, '0')
      return m + '-' + day
    }
  }
}
</script>

<style scoped>
.bell-wrapper { position: relative; display: inline-block; line-height: 50px; cursor: pointer; padding: 0 10px; }
.el-icon-bell { font-size: 20px; color: #5a5e66; transition: all 0.3s; }
.bell-badge { position: absolute; top: 6px; right: 2px; min-width: 16px; height: 16px; padding: 0 4px; border-radius: 8px; background: #f56c6c; color: #fff; font-size: 11px; line-height: 16px; text-align: center; border: 1px solid #fff; pointer-events: none; }
.el-icon-bell.has-unread { color: #e6a23c; animation: bellShake 2s infinite; }
@keyframes bellShake {
  0%, 100% { transform: rotate(0deg); }
  10%, 30% { transform: rotate(8deg); }
  20% { transform: rotate(-8deg); }
  40% { transform: rotate(0deg); }
}

.notification-dropdown { width: 380px; padding: 0 !important; margin-top: 5px !important; }

.notif-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px 10px; border-bottom: 1px solid #f0f0f0;
}
.notif-header-title { font-size: 15px; font-weight: 700; color: #303133; }
.notif-header-count { font-size: 12px; color: #909399; }

.notif-body { max-height: 400px; overflow-y: auto; }

.notif-item {
  display: flex; padding: 12px 16px; cursor: pointer;
  border-bottom: 1px solid #f5f5f5; transition: background 0.2s;
}
.notif-item:hover { background: #f5f7fa; }
.notif-item.notif-unread { background: #fef9f0; }
.notif-item.notif-unread:hover { background: #fdf6ec; }

.notif-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #f56c6c; margin-top: 6px; margin-right: 10px; flex-shrink: 0;
}
.notif-content-wrap { flex: 1; min-width: 0; }

.notif-title-row {
  display: flex; justify-content: space-between; align-items: center;
}
.notif-title { font-size: 13px; font-weight: 600; color: #303133; }
.notif-item:not(.notif-unread) .notif-title { font-weight: 400; color: #606266; }
.notif-time { font-size: 11px; color: #c0c4cc; white-space: nowrap; margin-left: 8px; }

.notif-desc { font-size: 12px; color: #909399; margin-top: 3px; line-height: 1.4; }

.notif-empty {
  text-align: center; padding: 40px 0; color: #c0c4cc; font-size: 13px;
}

.notif-footer {
  display: flex; justify-content: space-between; padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
}
.notif-footer .el-button { font-size: 12px; padding: 0; }
</style>