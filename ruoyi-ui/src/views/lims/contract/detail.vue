<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="合同详情" />
    <div v-loading="loading" style="margin-top:16px;">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="合同编号">{{ detail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusTag(detail.status)">{{ statusLabel(detail.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="合同名称" :span="2">{{ detail.contractName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ detail.contractType }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">{{ detail.currency || "CNY" }} {{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="甲方">{{ detail.partyA }}</el-descriptions-item><el-descriptions-item label="乙方">{{ detail.partyB }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detail.startDate }}</el-descriptions-item><el-descriptions-item label="到期日期">{{ detail.endDate }}</el-descriptions-item>
        <el-descriptions-item label="合同内容" :span="2">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
      <h3 style="margin:20px 0 10px;">合同附件</h3>
      <el-table :data="attachmentList" border v-loading="loadingFile">
        <el-table-column label="文件名" prop="fileName" />
        <el-table-column label="文件大小" width="120">
          <template slot-scope="s">{{ (s.row.fileSize / 1024).toFixed(1) + ' KB' }}</template>
        </el-table-column>
        <el-table-column label="上传人" prop="createBy" width="100" />
        <el-table-column label="操作" width="200">
          <template slot-scope="s">
            <el-button type="text" icon="el-icon-view" @click="handlePreview(s.row)">预览</el-button>
            <el-button type="text" icon="el-icon-download" @click="handleDownload(s.row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
import { getContract } from "@/api/lims/contract"
import { listByBatch } from "@/api/lims/sysFile"
export default {
  name: "ContractDetail",
  data() { return { loading: false, loadingFile: false, detail: {}, attachmentList: [] } },
  created() { 
    const id = this.$route.params.id; 
    if (id) { 
      this.loading = true; 
      // getContract(id).then(r => { this.detail = r.data; this.loading = false }) 
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
      var fileUrl = btoa('http://127.0.0.1:8080' + f.ossUrl);
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
 goBack() { this.$router.push("/lims/contract") },
    statusTag(s) { return { "0":"info","1":"primary","2":"success","3":"danger","4":"warning" }[s] || "" },
    statusLabel(s) { return { "0":"草稿","1":"审批中","2":"已通过","3":"已驳回","4":"已过期" }[s] || "" }
  }
}
</script>
