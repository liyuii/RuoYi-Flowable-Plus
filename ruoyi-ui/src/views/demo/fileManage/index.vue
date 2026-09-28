<template>
  <div class="app-container">
    <!-- 查询条件：只看自己上传的文件 -->
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true" size="small" label-width="80px">
      <el-form-item label="项目名称" prop="projectName">
        <el-input
          v-model="queryParams.projectName"
          placeholder="请输入项目名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所在省份" prop="provinceCode">
        <el-select v-model="queryParams.provinceCode" placeholder="请选择省份" clearable style="width: 160px">
          <el-option v-for="p in provinceOptions" :key="p.code" :label="p.name" :value="p.code" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="item in auditStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-upload2" size="mini" @click="handleAdd">上传投标方案</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="fileList" border>
      <el-table-column label="项目名称" align="left" prop="projectName" min-width="180" show-overflow-tooltip />
      <el-table-column label="所在省份" align="center" width="120">
        <template slot-scope="scope">{{ provinceName(scope.row.provinceCode) }}</template>
      </el-table-column>
      <el-table-column label="金额（元）" align="right" width="130">
        <template slot-scope="scope">{{ formatAmount(scope.row.amount) }}</template>
      </el-table-column>
      <el-table-column label="文件名称" align="left" prop="fileName" min-width="200" show-overflow-tooltip />
      <el-table-column label="大小" align="center" width="100">
        <template slot-scope="scope">{{ formatSize(scope.row.fileSize) }}</template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" width="110">
        <template slot-scope="scope">
          <el-tag size="mini" :type="auditTag(scope.row.auditStatus)">{{ auditText(scope.row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核原因" align="left" min-width="180" show-overflow-tooltip>
        <template slot-scope="scope">
          <span v-if="scope.row.auditStatus === '2'">{{ scope.row.auditReason || '-' }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="上传时间" align="center" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260">
        <template slot-scope="scope">
          <el-button
            type="text"
            icon="el-icon-s-check"
            size="mini"
            :disabled="scope.row.auditStatus !== '0'"
            @click="handleAudit(scope.row)"
          >审核</el-button>
          <el-button
            type="text"
            icon="el-icon-edit"
            size="mini"
            :disabled="scope.row.auditStatus !== '2'"
            @click="handleEdit(scope.row)"
          >编辑</el-button>
          <el-button type="text" icon="el-icon-download" size="mini" @click="handleDownload(scope.row)">下载</el-button>
          <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 上传 / 编辑表单 -->
    <el-dialog
      :title="formTitle"
      :visible.sync="formOpen"
      width="640px"
      append-to-body
      :close-on-click-modal="false"
      @close="handleFormClose"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" maxlength="255" />
        </el-form-item>
        <el-form-item label="项目金额" prop="amount">
          <el-input v-model="form.amount" placeholder="请输入金额">
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="所在省份" prop="provinceCode">
          <el-select v-model="form.provinceCode" placeholder="请选择省份" style="width: 100%">
            <el-option v-for="p in provinceOptions" :key="p.code" :label="p.name" :value="p.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="投标方案文件">
          <div class="file-picker">
            <el-button size="mini" type="primary" icon="el-icon-upload2" :disabled="uploading" @click="chooseFile">选择文件</el-button>
            <template v-if="form.fileName">
              <span class="file-name" :title="form.fileName">{{ form.fileName }}</span>
              <el-button
                type="text"
                icon="el-icon-delete"
                size="mini"
                class="file-remove"
                :disabled="uploading"
                @click="handleRemoveFile"
              >删除</el-button>
            </template>
            <span v-else class="file-name empty">未选择文件</span>
          </div>
          <div class="file-tip">
            仅支持 .docx 格式，单个文件不超过 50MB；文件先存本地，审核通过后才会上传 OSS、转成预览图片并发布到投标方案列表
          </div>
          <input ref="fileInput" type="file" style="display: none" @change="onFileChange">
        </el-form-item>
        <el-form-item v-if="task" label="上传进度">
          <div class="upload-panel">
            <div class="upload-head">
              <span class="file-name" :title="task.fileName">{{ task.fileName }}</span>
              <el-tag size="mini" :type="statusTag">{{ statusText }}</el-tag>
            </div>
            <el-progress
              :percentage="task.percent"
              :status="progressStatus"
              :stroke-width="12"
              style="margin: 8px 0"
            />
            <div class="upload-meta">{{ task.percent }}% {{ task.completedChunks.length }}/{{ task.totalChunks }} 分片</div>
            <div class="upload-actions">
              <el-button v-if="task.status === 'uploading'" size="mini" icon="el-icon-video-pause" @click="pauseUpload">暂停</el-button>
              <el-button v-if="task.status === 'paused' && task.file" size="mini" type="primary" icon="el-icon-video-play" @click="resumeUpload">继续</el-button>
              <el-button v-if="task.uploadId && task.status !== 'merging'" size="mini" type="danger" icon="el-icon-close" @click="cancelUploadTask">取消</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="formOpen = false">取 消</el-button>
        <el-button type="primary" size="mini" :loading="submitting" :disabled="uploading" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 审核 -->
    <el-dialog title="审核投标方案" :visible.sync="auditOpen" width="520px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="项目名称">
          <span>{{ auditForm.projectName }}</span>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.pass">
            <el-radio :label="true">通过（发布到投标方案列表）</el-radio>
            <el-radio :label="false">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditForm.pass === false" label="不通过原因">
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请填写不通过原因，用户可以在列表里看到"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="auditOpen = false">取 消</el-button>
        <el-button type="primary" size="mini" :loading="auditSubmitting" @click="submitAudit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  initUpload,
  uploadChunk,
  completeUpload,
  cancelUpload,
  listUploadFiles,
  downloadUploadFile,
  deleteUploadFile,
  editUploadFile,
  auditUploadFile
} from '@/api/demo/fileUpload'
import { PROVINCE_OPTIONS, getProvinceName } from '@/utils/province'

const AUDIT_WAIT = '0'
const AUDIT_PASS = '1'
const AUDIT_REJECT = '2'

// 金额按字符串校验，避免浮点精度问题
function validateAmount(rule, value, callback) {
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入项目金额'))
    return
  }
  if (!/^\d{1,16}(\.\d{1,2})?$/.test(String(value))) {
    callback(new Error('金额格式不正确，最多两位小数'))
    return
  }
  callback()
}

export default {
  name: 'FileManage',
  data() {
    return {
      loading: false,
      showSearch: true,
      fileList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectName: null,
        provinceCode: null,
        auditStatus: null
      },
      provinceOptions: PROVINCE_OPTIONS,
      auditStatusOptions: [
        { value: AUDIT_WAIT, label: '待审核' },
        { value: AUDIT_PASS, label: '审核通过' },
        { value: AUDIT_REJECT, label: '审核不通过' }
      ],
      formOpen: false,
      formTitle: '上传投标方案',
      isEdit: false,
      submitting: false,
      form: {},
      rules: {
        projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        amount: [
          { required: true, message: '请输入项目金额', trigger: 'blur' },
          { validator: validateAmount, trigger: 'blur' }
        ],
        provinceCode: [{ required: true, message: '请选择省份', trigger: 'change' }]
      },
      task: null,
      auditOpen: false,
      auditSubmitting: false,
      auditForm: { id: null, projectName: '', pass: null, reason: '' }
    }
  },
  computed: {
    uploading() {
      if (!this.task) {
        return false
      }
      return ['hashing', 'uploading', 'merging'].includes(this.task.status)
    },
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
      return this.task && this.task.status === 'success' ? 'success' : null
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
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams.projectName = null
      this.queryParams.provinceCode = null
      this.queryParams.auditStatus = null
      this.handleQuery()
    },
    provinceName(code) {
      return getProvinceName(code)
    },
    auditText(status) {
      const map = { 0: '待审核', 1: '审核通过', 2: '审核不通过' }
      return map[status] || '待审核'
    },
    auditTag(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'danger' }
      return map[status] || 'info'
    },
    formatAmount(amount) {
      if (amount === null || amount === undefined || amount === '') {
        return '0.00'
      }
      const parts = String(amount).split('.')
      const integer = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
      const decimal = (parts[1] || '').padEnd(2, '0').slice(0, 2)
      return integer + '.' + decimal
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
    },
    handleAdd() {
      this.isEdit = false
      this.formTitle = '上传投标方案'
      this.task = null
      this.form = {
        id: null,
        projectName: '',
        amount: '0.00',
        provinceCode: null,
        fileName: '',
        file: null,
        fileRemoved: false
      }
      this.formOpen = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    handleEdit(row) {
      this.isEdit = true
      this.formTitle = '编辑投标方案（审核不通过）'
      this.task = null
      this.form = {
        id: row.id,
        projectName: row.projectName,
        amount: row.amount === null || row.amount === undefined ? '0.00' : String(row.amount),
        provinceCode: row.provinceCode,
        fileName: row.fileName,
        file: null,
        fileRemoved: false
      }
      this.formOpen = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    chooseFile() {
      this.$refs.fileInput.click()
    },
    onFileChange(event) {
      const file = event.target.files && event.target.files[0]
      event.target.value = ''
      if (!file) {
        return
      }
      const name = (file.name || '').toLowerCase()
      if (!name.endsWith('.docx')) {
        this.$modal.msgError('只支持 .docx 格式')
        return
      }
      if (file.size / 1024 / 1024 > 50) {
        this.$modal.msgError('文件大小不能超过 50MB')
        return
      }
      this.form.file = file
      this.form.fileName = file.name
      this.form.fileRemoved = false
      this.task = null
    },
    handleRemoveFile() {
      this.form.file = null
      this.form.fileName = ''
      this.form.fileRemoved = true
      this.task = null
      this.$modal.msgWarning('已移除文件，请重新选择后再保存')
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (!this.form.file) {
          if (!this.isEdit) {
            this.$modal.msgError('请选择投标方案文件')
            return
          }
          if (this.form.fileRemoved) {
            this.$modal.msgError('已移除原文件，请重新选择投标方案文件')
            return
          }
          // 编辑时没有重新选文件：只更新表单信息，重新进入待审核
          this.submitting = true
          editUploadFile({
            id: this.form.id,
            projectName: this.form.projectName,
            amount: this.form.amount,
            provinceCode: this.form.provinceCode
          }).then(() => {
            this.$modal.msgSuccess('保存成功，已重新提交审核')
            this.formOpen = false
            this.getList()
          }).catch(() => {}).then(() => {
            this.submitting = false
          })
          return
        }
        this.submitting = true
        this.startUpload(this.form.file)
      })
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
        const initRes = await initUpload({
          projectName: this.form.projectName,
          amount: this.form.amount,
          provinceCode: this.form.provinceCode,
          replaceId: this.isEdit ? this.form.id : null,
          fileName: file.name,
          fileSize: file.size,
          fileHash: fileHash
        })
        const session = initRes.data
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
        this.submitting = false
        if (this.task && this.task.status !== 'merging' && this.task.status !== 'success') {
          this.task.status = 'paused'
        }
      }
    },
    computeHash(file) {
      if (!window.crypto || !window.crypto.subtle) {
        this.$modal.msgError('当前浏览器不支持文件哈希计算')
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
          this.submitting = false
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
          this.$modal.msgSuccess(this.isEdit ? '重新上传成功，已提交审核' : '上传成功，等待审核')
          this.submitting = false
          this.formOpen = false
          this.task = null
          this.getList()
        }
      } catch (error) {
        this.submitting = false
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
        this.submitting = false
      }
    },
    resumeUpload() {
      if (this.task && this.task.status === 'paused' && this.task.file) {
        this.submitting = true
        this.task.status = 'uploading'
        this.runChunks()
      }
    },
    cancelUploadTask() {
      const task = this.task
      if (!task || !task.uploadId) {
        this.task = null
        this.submitting = false
        return
      }
      this.$modal.confirm('确认取消本次上传？').then(() => {
        cancelUpload(task.uploadId).then(() => {
          this.$modal.msgSuccess('已取消')
          this.task = null
          this.submitting = false
        }).catch(() => {})
      }).catch(() => {})
    },
    handleFormClose() {
      if (this.task && this.task.status !== 'success') {
        this.cancelUploadTask()
      }
      this.task = null
      this.submitting = false
    },
    handleAudit(row) {
      this.auditForm = { id: row.id, projectName: row.projectName, pass: null, reason: '' }
      this.auditOpen = true
    },
    submitAudit() {
      if (this.auditForm.pass === null) {
        this.$modal.msgError('请选择审核结果')
        return
      }
      if (this.auditForm.pass === false && !String(this.auditForm.reason || '').trim()) {
        this.$modal.msgError('审核不通过必须填写原因')
        return
      }
      this.auditSubmitting = true
      auditUploadFile({
        id: this.auditForm.id,
        pass: this.auditForm.pass,
        reason: this.auditForm.reason
      }).then(() => {
        this.$modal.msgSuccess(this.auditForm.pass ? '审核通过，已发布到投标方案列表' : '已退回，用户可编辑后重新提交')
        this.auditOpen = false
        this.getList()
      }).catch(() => {}).then(() => {
        this.auditSubmitting = false
      })
    },
    async handleDownload(row) {
      try {
        const data = await downloadUploadFile(row.id)
        if (data.type && data.type.indexOf('application/json') !== -1) {
          this.$modal.msgError('下载失败')
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
        this.$modal.msgError('下载失败')
      }
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除「' + (row.projectName || row.fileName) + '」？删除后本地文件会一起清理。').then(() => {
        return deleteUploadFile(row.id)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.file-picker {
  display: flex;
  align-items: center;
  gap: 8px;
}
.file-name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}
.file-name.empty {
  color: #c0c4cc;
}
.file-remove {
  padding: 0;
  color: #f56c6c;
}
.file-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
.upload-panel {
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  padding: 10px 12px;
  background: #fafafa;
  width: 100%;
}
.upload-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.upload-meta {
  font-size: 12px;
  color: #909399;
}
.upload-actions {
  margin-top: 6px;
}
</style>
