<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-upload2"
          size="mini"
          :disabled="!!task"
          @click="chooseFile"
        >上传文件</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
      <input ref="fileInput" type="file" style="display: none" @change="onFileChange">
    </el-row>

    <div v-if="task" class="upload-panel">
      <div class="upload-head">
        <span class="upload-name">{{ task.fileName }}</span>
        <el-tag size="mini" :type="statusTag">{{ statusText }}</el-tag>
      </div>
      <el-progress
        :percentage="task.percent"
        :status="progressStatus"
        :stroke-width="12"
        style="margin: 8px 0"
      />
      <div class="upload-meta">
        {{ task.percent }}% {{ task.completedChunks.length }}/{{ task.totalChunks }} 分片
      </div>
      <div class="upload-actions">
        <el-button
          v-if="task.status === 'uploading'"
          size="mini"
          icon="el-icon-video-pause"
          @click="pauseUpload"
        >暂停</el-button>
        <el-button
          v-if="task.status === 'paused' && task.file"
          size="mini"
          type="primary"
          icon="el-icon-video-play"
          @click="resumeUpload"
        >继续</el-button>
        <el-button
          v-if="task.uploadId && task.status !== 'merging' && task.status !== 'success'"
          size="mini"
          type="danger"
          icon="el-icon-close"
          @click="cancelUploadTask"
        >取消</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="fileList" border>
      <el-table-column label="文件名" align="left" prop="fileName" min-width="260" show-overflow-tooltip />
      <el-table-column label="大小" align="center" width="120">
        <template slot-scope="scope">
          {{ formatSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="上传时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-download" @click="handleDownload(scope.row)">下载</el-button>
          <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import {
  initUpload,
  getUploadProgress,
  uploadChunk,
  completeUpload,
  cancelUpload,
  listUploadFiles,
  downloadUploadFile,
  deleteUploadFile
} from '@/api/demo/fileUpload'

export default {
  name: 'FileManage',
  data() {
    return {
      loading: false,
      fileList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10 },
      task: null
    }
  },
  computed: {
    statusText() {
      const map = {
        hashing: '校验中',
        uploading: '上传中',
        paused: '已暂停',
        merging: '合并中',
        success: '已完成'
      }
      return this.task ? map[this.task.status] || '上传中' : ''
    },
    statusTag() {
      const map = {
        uploading: 'primary',
        merging: 'warning',
        success: 'success',
        paused: 'info'
      }
      return this.task ? map[this.task.status] || 'info' : 'info'
    },
    progressStatus() {
      if (this.task && this.task.status === 'success') {
        return 'success'
      }
      return null
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listUploadFiles(this.queryParams).then(res => {
        this.fileList = res.rows || []
        this.total = res.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    chooseFile() {
      this.$refs.fileInput.click()
    },
    onFileChange(event) {
      const file = event.target.files && event.target.files[0]
      event.target.value = ''
      if (file) {
        this.startUpload(file)
      }
    },
    async startUpload(file) {
      this.task = {
        file: file,
        fileName: file.name,
        fileSize: file.size,
        fileHash: '',
        uploadId: '',
        chunkSize: 0,
        totalChunks: 0,
        completedChunks: [],
        percent: 0,
        status: 'hashing'
      }
      try {
        const fileHash = await this.computeHash(file)
        if (!this.task || this.task.file !== file) {
          return
        }
        this.task.fileHash = fileHash
        const progressRes = await getUploadProgress(fileHash)
        let session = progressRes.data
        if (!session || !session.uploadId) {
          const initRes = await initUpload({
            fileName: file.name,
            fileSize: file.size,
            fileHash: fileHash
          })
          session = initRes.data
        }
        if (!this.task || this.task.file !== file) {
          return
        }
        this.task.uploadId = session.uploadId
        this.task.chunkSize = session.chunkSize
        this.task.totalChunks = session.totalChunks
        this.task.completedChunks = session.uploadedChunkIndexes || []
        this.task.status = 'uploading'
        this.refreshPercent()
        await this.runChunks()
      } catch (error) {
        if (this.task && this.task.status !== 'merging' && this.task.status !== 'success') {
          this.task.status = 'paused'
        }
      }
    },
    computeHash(file) {
      if (!window.crypto || !window.crypto.subtle) {
        this.$message.error('当前浏览器不支持文件哈希计算')
        return Promise.reject(new Error('crypto unsupported'))
      }
      return file.arrayBuffer().then(buffer => {
        return window.crypto.subtle.digest('SHA-256', buffer)
      }).then(digest => {
        return Array.from(new Uint8Array(digest)).map(item => {
          return item.toString(16).padStart(2, '0')
        }).join('')
      })
    },
    async runChunks() {
      const task = this.task
      if (!task || !task.uploadId || !task.file) {
        return
      }
      task.status = 'uploading'
      for (let index = 0; index < task.totalChunks; index++) {
        if (this.task !== task || task.status !== 'uploading') {
          return
        }
        if (task.completedChunks.includes(index)) {
          continue
        }
        const start = index * task.chunkSize
        const end = Math.min(start + task.chunkSize, task.fileSize)
        const blob = task.file.slice(start, end)
        const formData = new FormData()
        formData.append('uploadId', task.uploadId)
        formData.append('chunkIndex', index)
        formData.append('file', blob, task.fileName + '.part')
        try {
          await uploadChunk(formData)
          if (this.task === task && !task.completedChunks.includes(index)) {
            task.completedChunks.push(index)
            this.refreshPercent()
          }
        } catch (error) {
          if (this.task === task) {
            task.status = 'paused'
          }
          return
        }
      }
      if (this.task !== task || task.status !== 'uploading') {
        return
      }
      task.status = 'merging'
      this.refreshPercent()
      try {
        await completeUpload({
          uploadId: task.uploadId,
          fileHash: task.fileHash
        })
        if (this.task === task) {
          task.status = 'success'
          task.percent = 100
          this.$message.success('上传完成')
          this.task = null
          this.getList()
        }
      } catch (error) {
        if (this.task === task) {
          task.status = 'paused'
        }
      }
    },
    refreshPercent() {
      const task = this.task
      if (!task || !task.totalChunks) {
        return
      }
      const done = Array.from(new Set(task.completedChunks)).length
      task.percent = Math.min(100, Math.floor(done / task.totalChunks * 100))
    },
    pauseUpload() {
      if (this.task && this.task.status === 'uploading') {
        this.task.status = 'paused'
      }
    },
    resumeUpload() {
      if (this.task && this.task.status === 'paused' && this.task.file) {
        this.task.status = 'uploading'
        this.runChunks()
      }
    },
    cancelUploadTask() {
      const task = this.task
      if (!task || !task.uploadId) {
        this.task = null
        return
      }
      this.$confirm('确认取消本次上传？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelUpload(task.uploadId).then(() => {
          this.$message.success('已取消')
          this.task = null
        }).catch(() => {})
      }).catch(() => {})
    },
    async handleDownload(row) {
      try {
        const data = await downloadUploadFile(row.id)
        if (data.type && data.type.indexOf('application/json') !== -1) {
          this.$message.error('下载失败')
          return
        }
        const url = URL.createObjectURL(new Blob([data], { type: 'application/octet-stream' }))
        const link = document.createElement('a')
        link.href = url
        link.download = row.fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
      } catch (error) {
        this.$message.error('下载失败')
      }
    },
    handleDelete(row) {
      this.$confirm('确认删除该文件？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteUploadFile(row.id).then(() => {
          this.$message.success('删除成功')
          this.getList()
        })
      }).catch(() => {})
    },
    formatSize(size) {
      if (!size && size !== 0) {
        return '-'
      }
      if (size < 1024) {
        return size + ' B'
      }
      if (size < 1024 * 1024) {
        return (size / 1024).toFixed(2) + ' KB'
      }
      if (size < 1024 * 1024 * 1024) {
        return (size / 1024 / 1024).toFixed(2) + ' MB'
      }
      return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB'
    }
  }
}
</script>

<style scoped>
.upload-panel {
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  padding: 12px 16px;
  margin-bottom: 12px;
  background: #fafafa;
}
.upload-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.upload-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}
.upload-meta {
  color: #666;
  font-size: 12px;
  margin-bottom: 8px;
}
.upload-actions {
  display: flex;
  gap: 4px;
}
</style>
