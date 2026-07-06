<template>
  <div class="process-info">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="流转记录" name="record">
        <el-card class="box-card" shadow="never" v-loading="loading">
          <el-col :span="22" :offset="1">
            <div class="block">
              <el-timeline>
                <el-timeline-item v-for="(item,index) in historyProcNodeList" :key="index" :icon="setIcon(item.endTime)" :color="setColor(item.endTime)">
                  <p style="font-weight: 700">{{ item.activityName }}</p>
                  <el-card v-if="item.activityType === 'startEvent'" class="box-card" shadow="hover">
                    {{ item.assigneeName }} 在 {{ item.createTime }} 发起流程
                  </el-card>
                  <el-card v-if="item.activityType === 'userTask'" class="box-card" shadow="hover">
                    <el-descriptions :column="4" :labelStyle="{'font-weight': 'bold'}">
                      <el-descriptions-item label="实际办理">{{ item.assigneeName || '-'}}</el-descriptions-item>
                      <el-descriptions-item label="候选办理">{{ item.candidate || '-'}}</el-descriptions-item>
                      <el-descriptions-item label="接收时间">{{ item.createTime || '-'}}</el-descriptions-item>
                      <el-descriptions-item label="办结时间">{{ item.endTime || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="耗时">{{ item.duration || '-'}}</el-descriptions-item>
                    </el-descriptions>
                    <div v-if="item.commentList && item.commentList.length > 0">
                      <div v-for="(comment, index) in item.commentList" :key="index">
                        <el-divider content-position="left">
                          <el-tag :type="approveTypeTag(comment.type)" size="mini">{{ commentType(comment.type) }}</el-tag>
                          <el-tag type="info" effect="plain" size="mini">{{ comment.time }}</el-tag>
                        </el-divider>
                        <span>{{ comment.fullMessage }}</span>
                      </div>
                    </div>
                  </el-card>
                  <el-card v-if="item.activityType === 'endEvent'" class="box-card" shadow="hover">
                    {{ item.createTime }} 结束流程
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-col>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="流程跟踪" name="track">
        <el-card class="box-card" shadow="never" v-loading="loading">
          <process-viewer :key="'proc-' + loadIndex" :style="'height:400px'" :xml="xmlData" :finishedInfo="finishedInfo" :allCommentList="historyProcNodeList" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { detailProcess } from '@/api/workflow/process'
import ProcessViewer from '@/components/ProcessViewer'

export default {
  name: 'ProcessInfo',
  components: { ProcessViewer },
  props: {
    procInsId: { type: String, default: '' },
    taskId: { type: String, default: '' }
  },
  data() {
    return {
      activeTab: 'record',
      loadIndex: 0,
      loading: false,
      xmlData: undefined,
      finishedInfo: {
        finishedSequenceFlowSet: [],
        finishedTaskSet: [],
        unfinishedTaskSet: [],
        rejectedTaskSet: []
      },
      historyProcNodeList: []
    }
  },
  watch: {
    procInsId: { immediate: true, handler: 'loadData' }
  },
  methods: {
    loadData() {
      if (!this.procInsId && !this.taskId) return
      this.loading = true
      const params = {}
      if (this.procInsId) params.procInsId = this.procInsId
      if (this.taskId) params.taskId = this.taskId
      detailProcess(params).then(res => {
        const data = res.data
        this.xmlData = data.bpmnXml
        this.historyProcNodeList = data.historyProcNodeList
        this.finishedInfo = data.flowViewer
        this.loadIndex = this.procInsId || this.taskId
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    setIcon(val) {
      return val ? 'el-icon-check' : 'el-icon-time'
    },
    setColor(val) {
      return val ? '#2bc418' : '#b3bdbb'
    },
    commentType(val) {
      const map = { '1': '通过', '2': '退回', '3': '驳回', '4': '委派', '5': '转办', '6': '终止', '7': '撤回' }
      return map[val] || val
    },
    approveTypeTag(val) {
      const map = { '1': 'success', '2': 'warning', '3': 'danger', '4': 'primary', '5': 'success', '6': 'danger', '7': 'info' }
      return map[val] || ''
    }
  }
}
</script>

<style scoped>
.box-card {
  width: 100%;
  margin-bottom: 20px;
}
.process-info .el-tabs {
  min-height: 300px;
}
</style>