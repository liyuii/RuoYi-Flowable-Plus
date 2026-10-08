<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width:160px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="extractList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="项目名称" prop="projectName" show-overflow-tooltip min-width="180" />
      <el-table-column label="标段" prop="sectionNames" show-overflow-tooltip min-width="140" />
      <el-table-column label="抽取地区" width="120">
        <template slot-scope="scope">{{ regionLabel(scope.row.regionCode) }}</template>
      </el-table-column>
      <el-table-column label="评标时间" prop="evaluateTime" width="160" />
      <el-table-column label="状态" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请人" prop="applyBy" width="100" />
      <el-table-column label="操作" width="300" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row, 'view')">详情</el-button>
          <el-button type="text" icon="el-icon-s-check" v-if="scope.row.status === 'WAIT_AUDIT'"
                     @click="handleDetail(scope.row, 'audit')">审核</el-button>
          <el-button type="text" icon="el-icon-edit" v-if="scope.row.status === 'REJECTED'"
                     @click="handleDetail(scope.row, 'edit')">编辑</el-button>
          <el-button type="text" icon="el-icon-magic-stick" v-if="scope.row.status === 'WAIT_EXTRACT'"
                     @click="handleDetail(scope.row, 'draw')">抽取</el-button>
          <el-button type="text" icon="el-icon-refresh-right"
                     v-if="scope.row.status === 'EXTRACTED' || scope.row.status === 'WAIT_SUPPLEMENT'"
                     @click="handleDetail(scope.row, 'supplement')">补充抽取</el-button>
          <el-button type="text" icon="el-icon-delete" v-if="canDelete(scope.row)" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="新增抽取申请" :visible.sync="dialogVisible" width="880px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <div class="section-title">基本信息</div>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标段名称">
              <el-input v-model="form.sectionNames" placeholder="多个标段用逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抽取地区" prop="regionCode">
              <el-select v-model="form.regionCode" placeholder="请选择抽取地区" style="width:100%">
                <el-option v-for="r in regionOptions" :key="r.dictValue" :label="r.dictLabel" :value="r.dictValue" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评标时间" prop="evaluateTime">
              <el-date-picker v-model="form.evaluateTime" type="datetime" placeholder="请选择评标时间"
                              value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="section-title">抽取范围</div>
        <el-table :data="form.scopes" border size="small">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="专业" min-width="240">
            <template slot-scope="scope">
              <el-select v-model="scope.row.majorCode" placeholder="请选择专业" style="width:100%"
                         :loading="countLoading" @visible-change="handleMajorVisible">
                <el-option v-for="m in majorOptions" :key="m.dictValue" :label="majorOptionLabel(m)" :value="m.dictValue" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="需要人数" width="190" align="center">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.needNum" :min="1" :max="99" size="small"
                               controls-position="right" style="width:130px" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="removeScope(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain size="small" icon="el-icon-plus" class="add-row-btn" @click="addScope">新增范围</el-button>

        <div class="section-title">回避名单</div>
        <el-table :data="form.expertAvoids" border size="small">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="专家姓名" min-width="160">
            <template slot-scope="scope">
              <el-input v-model="scope.row.expertName" placeholder="请输入专家姓名" />
            </template>
          </el-table-column>
          <el-table-column label="身份证号" min-width="220">
            <template slot-scope="scope">
              <el-input v-model="scope.row.idCard" placeholder="请输入身份证号" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="removeExpertAvoid(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain size="small" icon="el-icon-plus" class="add-row-btn" @click="addExpertAvoid">添加回避专家</el-button>

        <el-table :data="form.companyAvoids" border size="small" style="margin-top:12px;">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="单位名称" min-width="320">
            <template slot-scope="scope">
              <el-input v-model="scope.row.companyName" placeholder="请输入单位全称" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="removeCompanyAvoid(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain size="small" icon="el-icon-plus" class="add-row-btn" @click="addCompanyAvoid">添加回避单位</el-button>

        <div class="section-title">其他</div>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listExtract, addExtract, delExtract, majorCount } from '@/api/lims/expertExtract'
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'ExpertExtract',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      multiple: true,
      ids: [],
      extractList: [],
      majorOptions: [],
      regionOptions: [],
      majorCountMap: {},
      countLoading: false,
      dialogVisible: false,
      statusOptions: [
        { label: '待审核', value: 'WAIT_AUDIT' },
        { label: '待抽取', value: 'WAIT_EXTRACT' },
        { label: '已抽取', value: 'EXTRACTED' },
        { label: '补充抽取', value: 'WAIT_SUPPLEMENT' },
        { label: '审核驳回', value: 'REJECTED' }
      ],
      queryParams: { pageNum: 1, pageSize: 10, projectName: null, status: null },
      form: {},
      rules: {
        projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
        regionCode: [{ required: true, message: '请选择抽取地区', trigger: 'change' }],
        evaluateTime: [{ required: true, message: '请选择评标时间', trigger: 'change' }]
      }
    }
  },
  created() {
    getDicts('expert_major').then(res => { this.majorOptions = res.data || [] })
    getDicts('expert_region').then(res => { this.regionOptions = res.data || [] })
    this.getList()
  },
  methods: {
    regionLabel(code) {
      const item = this.regionOptions.find(r => r.dictValue === code)
      return item ? item.dictLabel : code
    },
    statusLabel(status) {
      const item = this.statusOptions.find(s => s.value === status)
      if (item) {
        return item.label
      }
      return { DRAFT: '草稿' }[status] || status
    },
    statusTag(status) {
      return {
        DRAFT: 'info',
        WAIT_AUDIT: 'warning',
        WAIT_EXTRACT: 'primary',
        EXTRACTED: 'success',
        WAIT_SUPPLEMENT: 'danger',
        REJECTED: 'info'
      }[status] || ''
    },
    canDelete(row) {
      return row.status === 'DRAFT' || row.status === 'WAIT_AUDIT' || row.status === 'REJECTED'
    },
    majorOptionLabel(item) {
      const count = this.majorCountMap[item.dictValue]
      return count === undefined || count === null ? item.dictLabel : item.dictLabel + '（' + count + '）'
    },
    handleMajorVisible(visible) {
      if (!visible || !this.form.regionCode || !this.form.evaluateTime) {
        return
      }
      const expertKeys = (this.form.expertAvoids || [])
        .filter(a => a.expertName && a.idCard)
        .map(a => a.expertName + '|' + a.idCard)
      const companies = (this.form.companyAvoids || [])
        .filter(a => a.companyName)
        .map(a => a.companyName)
      this.countLoading = true
      majorCount({
        regionCode: this.form.regionCode,
        evaluateTime: this.form.evaluateTime,
        expertKeys: expertKeys,
        companies: companies
      }).then(res => {
        const map = {}
        const list = res.data || []
        list.forEach(item => { map[item.majorCode] = item.availableNum })
        this.majorCountMap = map
        this.countLoading = false
      }).catch(() => { this.countLoading = false })
    },
    getList() {
      this.loading = true
      listExtract(this.queryParams).then(res => {
        this.extractList = res.rows
        this.total = res.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.$refs.queryRef.resetFields()
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    addScope() {
      this.form.scopes.push({ majorCode: null, needNum: 1 })
    },
    removeScope(index) {
      this.form.scopes.splice(index, 1)
    },
    addExpertAvoid() {
      this.form.expertAvoids.push({ expertName: null, idCard: null })
    },
    removeExpertAvoid(index) {
      this.form.expertAvoids.splice(index, 1)
    },
    addCompanyAvoid() {
      this.form.companyAvoids.push({ companyName: null })
    },
    removeCompanyAvoid(index) {
      this.form.companyAvoids.splice(index, 1)
    },
    handleAdd() {
      this.form = {
        projectName: null,
        sectionNames: null,
        regionCode: null,
        evaluateTime: null,
        remark: null,
        scopes: [{ majorCode: null, needNum: 1 }],
        expertAvoids: [],
        companyAvoids: []
      }
      this.majorCountMap = {}
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) {
          return
        }
        const scopes = this.form.scopes.filter(s => s.majorCode && s.needNum > 0)
        if (!scopes.length) {
          this.$modal.msgError('请至少填写一条完整的抽取范围')
          return
        }
        const avoids = []
        this.form.expertAvoids.forEach(a => {
          if (a.expertName && a.idCard) {
            avoids.push({ avoidType: 'EXPERT', expertName: a.expertName, idCard: a.idCard })
          }
        })
        this.form.companyAvoids.forEach(a => {
          if (a.companyName) {
            avoids.push({ avoidType: 'COMPANY', companyName: a.companyName })
          }
        })
        addExtract({
          projectName: this.form.projectName,
          sectionNames: this.form.sectionNames,
          regionCode: this.form.regionCode,
          evaluateTime: this.form.evaluateTime,
          remark: this.form.remark,
          scopes: scopes,
          avoids: avoids
        }).then(() => {
          this.$modal.msgSuccess('已提交审核')
          this.dialogVisible = false
          this.getList()
        })
      })
    },
    handleDetail(row, mode) {
      this.$router.push({ path: '/expert/extract-detail', query: { id: row.id, mode: mode || 'view' } })
    },
    handleDelete(row) {
      const deleteIds = row.id ? [row.id] : this.ids
      this.$modal.confirm('确认删除选中的抽取申请吗？').then(() => delExtract(deleteIds)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    }
  }
}
</script>

<style scoped>
.section-title {
  margin: 20px 0 12px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
  font-size: 15px;
  font-weight: 600;
  line-height: 16px;
  color: #303133;
}

.section-title:first-child {
  margin-top: 0;
}

.add-row-btn {
  margin-top: 10px;
}
</style>
