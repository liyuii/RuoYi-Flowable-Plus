<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="合同审批" />
    <div v-loading="loading" style="margin-top:16px;">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="合同编号">{{ detail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusTag(detail.status)">{{ statusLabel(detail.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="合同名称" :span="2">{{ detail.contractName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ detail.contractType }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">{{ detail.currency || "CNY" }} {{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="甲方">{{ detail.partyA }}</el-descriptions-item><el-descriptions-item label="乙方">{{ detail.partyB }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ detail.content }}</el-descriptions-item>
      </el-descriptions>
      <el-card style="margin-top:20px;">
        <div slot="header"><span>审批操作</span></div>
        <el-form ref="taskForm" :model="taskForm" label-width="100px">
          <el-form-item label="审批意见" prop="comment"><el-input type="textarea" :rows="4" v-model="taskForm.comment" placeholder="请输入审批意见" /></el-form-item>
        </el-form>
        <el-row type="flex" justify="center" :gutter="20">
          <el-button type="success" icon="el-icon-circle-check" @click="handleComplete" :loading="submitting">通过</el-button>
          <el-button type="danger" icon="el-icon-circle-close" @click="handleReject" :loading="submitting">驳回</el-button>
        </el-row>
      </el-card>
    </div>
  </div>
</template>
<script>
import { getContract } from "@/api/lims/contract"
import { complete, rejectTask } from "@/api/workflow/task"
export default {
  name: "ContractDetailAudit",
  data() { return { loading: false, submitting: false, detail: {}, taskForm: { comment: "", taskId: "" } } },
  created() {
    const taskId = this.$route.query.taskId; const businessKey = this.$route.query.businessKey || ""
    const id = businessKey.replace("contract_", "")
    if (id && taskId) { this.loading = true; this.taskForm.taskId = taskId; getContract(id).then(r => { this.detail = r.data; this.loading = false }) }
  },
  methods: {
    goBack() { this.$router.push("/lims/contract") },
    statusTag(s) { return { "0":"info","1":"primary","2":"success","3":"danger" }[s] || "" },
    statusLabel(s) { return { "0":"草稿","1":"审批中","2":"已通过","3":"已驳回" }[s] || "" },
    handleComplete() { if (!this.taskForm.comment) { this.$modal.msgWarning("请输入审批意见"); return } this.submitting = true; complete({ taskId: this.taskForm.taskId, comment: this.taskForm.comment }).then(r => { this.$modal.msgSuccess(r.msg); this.$router.push("/lims/contract") }).finally(() => { this.submitting = false }) },
    handleReject() { if (!this.taskForm.comment) { this.$modal.msgWarning("请输入审批意见"); return } this.submitting = true; rejectTask({ taskId: this.taskForm.taskId, comment: this.taskForm.comment }).then(r => { this.$modal.msgSuccess(r.msg); this.$router.push("/lims/contract") }).finally(() => { this.submitting = false }) }
  }
}
</script>