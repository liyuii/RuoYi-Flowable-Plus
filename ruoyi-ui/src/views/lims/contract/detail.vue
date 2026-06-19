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
    </div>
  </div>
</template>
<script>
import { getContract } from "@/api/lims/contract"
export default {
  name: "ContractDetail",
  data() { return { loading: false, detail: {} } },
  created() { const id = this.$route.params.id; if (id) { this.loading = true; getContract(id).then(r => { this.detail = r.data; this.loading = false }) } },
  methods: {
    goBack() { this.$router.push("/lims/contract") },
    statusTag(s) { return { "0":"info","1":"primary","2":"success","3":"danger","4":"warning" }[s] || "" },
    statusLabel(s) { return { "0":"草稿","1":"审批中","2":"已通过","3":"已驳回","4":"已过期" }[s] || "" }
  }
}
</script>