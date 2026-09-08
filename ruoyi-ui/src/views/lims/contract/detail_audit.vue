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
      <h3 style="margin:20px 0 10px;">合同附件</h3>
      <el-table :data="attachmentList" border v-loading="loadingFile">
        <el-table-column label="文件名" prop="fileName" />
        <el-table-column label="文件大小" width="120">
          <template slot-scope="s">{{ (s.row.fileSize / 1024).toFixed(1) + ' KB' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="s">
            <el-button type="text" icon="el-icon-view" @click="handlePreview(s.row)">预览</el-button>
            <el-button type="text" icon="el-icon-download" @click="handleDownload(s.row)">下载</el-button>
            <el-button v-if="isEditable(s.row)" type="text" icon="el-icon-edit" @click="handleEdit(s.row)">在线编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { listByBatch, getEditorConfig } from "@/api/lims/sysFile"
import { complete, rejectTask } from "@/api/workflow/task"
export default {
  name: "ContractDetailAudit",
  data() { return { loading: false, submitting: false, loadingFile: false, detail: {}, taskForm: { comment: "", taskId: "", procInsId: "" }, attachmentList: [] } },
  created() {
    const taskId = this.$route.query.taskId; const businessKey = this.$route.query.businessKey || ""
    const id = businessKey.replace("contract_", "")
    if (id && taskId) { 
      this.loading = true; 
      this.taskForm.taskId = taskId; this.taskForm.procInsId = this.$route.query.procInsId || ""; 
      getContract(id).then(r => { 
        this.detail = r.data; 
        this.loading = false; 
        if (r.data.attachmentBatch) {
           this.loadingFile = true; 
           listByBatch(r.data.attachmentBatch).then(r2 => { this.attachmentList = r2.data || r2.rows || []; this.loadingFile = false }) 
          } 
        }
      ) 
    }
  },
  methods: {
    handlePreview(f) {
      var fileUrl = btoa('http://127.0.0.1:8082' + f.ossUrl);
      window.open('http://127.0.0.1:8012/onlinePreview?url=' + fileUrl, '_blank');
    },
   handleDownload(f) {
      const a = document.createElement('a');
      a.href = '/dev-api' + f.ossUrl;
      a.download = f.fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    },
    goBack() { this.$router.back() },
    statusTag(s) { return { "0":"info","1":"primary","9":"success","3":"danger" }[s] || "" },
    statusLabel(s) { return { "0":"草稿","1":"审批中","9":"已通过","3":"已驳回" }[s] || "" },
    isEditable(row) {
      var suffix = (row.fileSuffix || "").toLowerCase();
      return suffix === ".docx" || suffix === ".doc";
    },
    handleEdit(row) {
      var url = this.$router.resolve("/editor/" + row.id).href;
      window.open(url, "_blank");
    },
    handleComplete() { if (!this.taskForm.comment) { this.$modal.msgWarning("请输入审批意见"); return } this.submitting = true; complete({ taskId: this.taskForm.taskId, procInsId: this.taskForm.procInsId, comment: this.taskForm.comment }).then(r => { this.$modal.msgSuccess(r.msg); this.$router.back() }).finally(() => { this.submitting = false }) },
    handleReject() { if (!this.taskForm.comment) { this.$modal.msgWarning("请输入审批意见"); return } this.submitting = true; rejectTask({ taskId: this.taskForm.taskId, procInsId: this.taskForm.procInsId, comment: this.taskForm.comment }).then(r => { this.$modal.msgSuccess(r.msg); this.$router.back() }).finally(() => { this.submitting = false }) }
  }
}
</script>
