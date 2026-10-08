<template>
  <div class="app-container">
    <div class="page-header">
      <el-page-header @back="goBack" :content="pageTitle" />
      <el-tag v-if="detail.status" :type="statusTag(detail.status)" size="medium" effect="plain">
        {{ statusLabel(detail.status) }}
      </el-tag>
    </div>

    <div v-loading="loading">
      <!-- 基本信息 -->
      <div class="section-title">基本信息</div>
      <el-form v-if="canEditBase" ref="formRef" :model="form" :rules="rules" label-width="90px">
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
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-descriptions v-else :column="3" border size="medium">
        <el-descriptions-item label="项目名称" :span="2">{{ detail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="标段">{{ detail.sectionNames || '-' }}</el-descriptions-item>
        <el-descriptions-item label="抽取地区">{{ regionLabel(detail.regionCode) }}</el-descriptions-item>
        <el-descriptions-item label="评标时间">{{ detail.evaluateTime }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applyBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.auditBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detail.auditTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ detail.auditOpinion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 抽取范围 -->
      <div class="section-title">抽取范围</div>
      <template v-if="canEditScope">
        <el-table :data="form.scopes" border size="small">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="专业" min-width="220">
            <template slot-scope="scope">
              <el-select v-model="scope.row.majorCode" :disabled="!canChangeMajor" placeholder="请选择专业"
                         style="width:100%" :loading="countLoading" @visible-change="handleMajorVisible">
                <el-option v-for="m in majorOptions" :key="m.dictValue" :label="majorOptionLabel(m)" :value="m.dictValue" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="需要人数" width="190" align="center">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.needNum" :min="scopeMin(scope.row)" :max="99"
                               size="small" controls-position="right" style="width:130px" />
            </template>
          </el-table-column>
          <el-table-column label="已抽到" width="100" align="center">
            <template slot-scope="scope">
              <span :class="{ 'text-warning': scope.row.validNum > 0 }">{{ scope.row.validNum || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="canRemoveScope" label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" @click="removeScope(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain size="small" icon="el-icon-plus" class="add-row-btn" @click="addScope">
          新增范围
        </el-button>
        <div v-if="mode === 'supplement'" class="section-tip">
          只能改数量或者新增范围。不能删范围、不能改已有范围的专业。数量不能小于已抽到的人数，要减人请先在抽取结果里把专家改成不参加。
        </div>
      </template>
      <el-table v-else :data="detail.scopes" border size="small">
        <el-table-column label="专业" min-width="180">
          <template slot-scope="scope">{{ scope.row.majorName || scope.row.majorCode }}</template>
        </el-table-column>
        <el-table-column label="需要人数" prop="needNum" width="120" align="center" />
        <el-table-column label="已抽到" prop="validNum" width="120" align="center" />
        <el-table-column label="还缺" width="120" align="center">
          <template slot-scope="scope">
            <span :class="scope.row.shortNum > 0 ? 'text-danger' : 'text-success'">{{ scope.row.shortNum }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 回避名单 -->
      <div class="section-title">回避名单</div>
      <template v-if="canEditAvoid">
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
        <el-button type="primary" plain size="small" icon="el-icon-plus" class="add-row-btn" @click="addExpertAvoid">
          添加回避专家
        </el-button>

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
        <el-button type="primary" plain size="small" icon="el-icon-plus" class="add-row-btn" @click="addCompanyAvoid">
          添加回避单位
        </el-button>
      </template>
      <el-table v-else :data="detail.avoids" border size="small">
        <el-table-column label="类型" width="90" align="center">
          <template slot-scope="scope">{{ scope.row.avoidType === 'EXPERT' ? '专家' : '单位' }}</template>
        </el-table-column>
        <el-table-column label="专家姓名" prop="expertName" width="140" />
        <el-table-column label="身份证号" prop="idCard" min-width="200" />
        <el-table-column label="单位名称" prop="companyName" min-width="240" />
      </el-table>

      <!-- 抽取结果 -->
      <template v-if="detail.results && detail.results.length">
        <div class="section-title">抽取结果</div>
        <el-table :data="detail.results" border size="small">
          <el-table-column label="专业" width="130">
            <template slot-scope="scope">{{ scope.row.majorName || scope.row.majorCode }}</template>
          </el-table-column>
          <el-table-column label="专家姓名" prop="expertName" width="110" />
          <el-table-column label="联系电话" prop="phone" width="130" />
          <el-table-column label="工作单位" prop="companyName" show-overflow-tooltip min-width="150" />
          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="resultStatusTag(scope.row.status)" size="small">
                {{ resultStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="理由" prop="reason" show-overflow-tooltip min-width="110" />
          <el-table-column label="轮次" width="110" align="center">
            <template slot-scope="scope">
              {{ scope.row.extractType === 'SUPPLEMENT' ? '第' + scope.row.roundNo + '轮补抽' : '第' + scope.row.roundNo + '轮抽取' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center">
            <template slot-scope="scope">
              <div v-if="canChangeStatus && canOperate(scope.row)">
                <el-button type="text" @click="changeStatus(scope.row, 'JOIN')">参加</el-button>
                <el-button type="text" @click="changeStatus(scope.row, 'QUIT')">不参加</el-button>
                <el-button type="text" @click="changeStatus(scope.row, 'LEAVE')">请假</el-button>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 底部操作栏 -->
      <div class="action-bar" v-if="showActionBar">
        <el-button v-if="mode === 'audit'" type="success" icon="el-icon-check" @click="handleAudit(true)">审核通过</el-button>
        <el-button v-if="mode === 'audit'" type="danger" icon="el-icon-close" @click="handleAudit(false)">退回</el-button>
        <el-button v-if="canEditScope" type="primary" icon="el-icon-document-checked" @click="handleSave">保存</el-button>
        <el-button v-if="mode === 'draw'" type="warning" icon="el-icon-magic-stick" @click="handleDraw">执行抽取</el-button>
        <el-button v-if="mode === 'supplement'" type="warning" icon="el-icon-refresh-right" @click="handleSupplement">补充抽取</el-button>
        <el-button icon="el-icon-back" @click="goBack">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getExtract, updateExtract, auditExtract, drawExtract, supplementExtract, updateResultStatus, majorCount } from '@/api/lims/expertExtract'
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'ExpertExtractDetail',
  data() {
    return {
      id: null,
      mode: 'view',
      loading: false,
      detail: {},
      form: {},
      majorOptions: [],
      regionOptions: [],
      majorCountMap: {},
      countLoading: false,
      rules: {
        projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
        regionCode: [{ required: true, message: '请选择抽取地区', trigger: 'change' }],
        evaluateTime: [{ required: true, message: '请选择评标时间', trigger: 'change' }]
      }
    }
  },
  computed: {
    pageTitle() {
      return {
        view: '抽取申请详情',
        audit: '抽取申请审核',
        edit: '修改抽取申请',
        draw: '执行抽取',
        supplement: '补充抽取'
      }[this.mode] || '抽取申请详情'
    },
    canEditBase() {
      return this.mode === 'edit' || this.mode === 'draw'
    },
    canEditScope() {
      return this.mode === 'edit' || this.mode === 'draw' || this.mode === 'supplement'
    },
    canChangeMajor() {
      return this.mode === 'edit' || this.mode === 'draw'
    },
    canRemoveScope() {
      return this.mode === 'edit' || this.mode === 'draw'
    },
    canEditAvoid() {
      return this.mode === 'edit' || this.mode === 'draw'
    },
    showActionBar() {
      return this.mode === 'audit' || this.canEditScope
    },
    canChangeStatus() {
      return this.detail.status === 'EXTRACTED' || this.detail.status === 'WAIT_SUPPLEMENT'
    }
  },
  created() {
    this.id = this.$route.query.id
    this.mode = this.$route.query.mode || 'view'
    getDicts('expert_major').then(res => { this.majorOptions = res.data || [] })
    getDicts('expert_region').then(res => { this.regionOptions = res.data || [] })
    this.getDetail()
  },
  methods: {
    goBack() {
      this.$router.push('/expert/extract')
    },
    backToList(message) {
      this.$modal.msgSuccess(message)
      this.$router.push('/expert/extract')
    },
    regionLabel(code) {
      const item = this.regionOptions.find(r => r.dictValue === code)
      return item ? item.dictLabel : code
    },
    statusLabel(status) {
      return {
        DRAFT: '草稿',
        WAIT_AUDIT: '待审核',
        WAIT_EXTRACT: '待抽取',
        EXTRACTED: '已抽取',
        WAIT_SUPPLEMENT: '补充抽取',
        REJECTED: '审核驳回'
      }[status] || status
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
    resultStatusLabel(status) {
      return {
        WAIT_CONFIRM: '待确认',
        JOIN: '参加',
        QUIT: '不参加',
        LEAVE: '请假'
      }[status] || status
    },
    resultStatusTag(status) {
      return {
        WAIT_CONFIRM: 'info',
        JOIN: 'success',
        QUIT: 'danger',
        LEAVE: 'warning'
      }[status] || ''
    },
    canOperate(row) {
      return row.status === 'WAIT_CONFIRM' || row.status === 'JOIN'
    },
    scopeMin(row) {
      // 已经抽过人的范围，数量不能小于已抽到的人数；还没抽的阶段最小是 1
      return this.canChangeMajor ? 1 : (row.validNum || 0)
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
    getDetail() {
      if (!this.id) {
        this.$modal.msgError('缺少申请ID')
        return
      }
      this.loading = true
      getExtract(this.id).then(res => {
        const data = res.data || {}
        this.detail = data
        this.form = {
          id: data.id,
          projectName: data.projectName,
          sectionNames: data.sectionNames,
          regionCode: data.regionCode,
          evaluateTime: data.evaluateTime,
          remark: data.remark,
          scopes: (data.scopes || []).map(s => ({
            id: s.id,
            majorCode: s.majorCode,
            needNum: s.needNum,
            validNum: s.validNum || 0
          })),
          expertAvoids: (data.avoids || [])
            .filter(a => a.avoidType === 'EXPERT')
            .map(a => ({ expertName: a.expertName, idCard: a.idCard })),
          companyAvoids: (data.avoids || [])
            .filter(a => a.avoidType === 'COMPANY')
            .map(a => ({ companyName: a.companyName }))
        }
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    addScope() {
      this.form.scopes.push({ id: null, majorCode: null, needNum: 1, validNum: 0 })
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
    buildPayload() {
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
      return {
        id: this.form.id,
        projectName: this.form.projectName,
        sectionNames: this.form.sectionNames,
        regionCode: this.form.regionCode,
        evaluateTime: this.form.evaluateTime,
        remark: this.form.remark,
        scopes: this.form.scopes.map(s => ({ id: s.id, majorCode: s.majorCode, needNum: s.needNum })),
        avoids: avoids
      }
    },
    saveForm() {
      return new Promise((resolve, reject) => {
        const submit = () => updateExtract(this.buildPayload()).then(() => resolve()).catch(reject)
        if (this.$refs.formRef) {
          this.$refs.formRef.validate(valid => {
            if (valid) {
              submit()
            } else {
              reject(new Error('validate'))
            }
          })
        } else {
          submit()
        }
      })
    },
    handleSave() {
      this.loading = true
      this.saveForm().then(() => {
        this.backToList('保存成功')
      }).catch(() => { this.loading = false })
    },
    showShortfall(checks) {
      const lines = (checks || [])
        .filter(c => c.shortNum > 0)
        .map(c => (c.majorName || c.majorCode) + ' 需要 ' + c.needNum + ' 人，可用 ' + c.availableNum + ' 人，缺 ' + c.shortNum + ' 人')
      this.$modal.alertWarning(lines.length ? lines.join('；') : '候选专家人数不足')
    },
    handleDraw() {
      this.$modal.confirm('确认执行抽取吗？抽取结果一旦产生，申请条件就不能随便改了。').then(() => {
        this.loading = true
        return this.saveForm().then(() => drawExtract(this.id))
      }).then(res => {
        const data = res.data || {}
        if (data.success) {
          this.backToList('抽取完成')
        } else {
          this.loading = false
          this.showShortfall(data.checks)
        }
      }).catch(() => { this.loading = false })
    },
    handleSupplement() {
      this.$modal.confirm('确认对缺少的人进行补充抽取吗？').then(() => {
        this.loading = true
        return this.saveForm().then(() => supplementExtract(this.id))
      }).then(res => {
        const data = res.data || {}
        if (data.success) {
          this.backToList('补充抽取完成')
        } else {
          this.loading = false
          this.showShortfall(data.checks)
        }
      }).catch(() => { this.loading = false })
    },
    handleAudit(pass) {
      const tip = pass ? '请输入审核意见，可以不填' : '请输入退回理由'
      this.$modal.prompt(tip).then(({ value }) => {
        if (!pass && !value) {
          this.$modal.msgError('退回必须填写理由')
          return
        }
        auditExtract({ id: this.id, pass: pass, opinion: value }).then(() => {
          this.backToList(pass ? '已通过审核' : '已退回')
        })
      }).catch(() => {})
    },
    changeStatus(row, status) {
      if (status === 'JOIN') {
        this.$modal.confirm('确认这位专家参加本次评标吗？').then(() => {
          this.doChangeStatus(row, status, null)
        }).catch(() => {})
        return
      }
      const tip = status === 'LEAVE' ? '请填写请假理由，必填' : '可以填写不参加的理由，也可以留空'
      this.$modal.prompt(tip).then(({ value }) => {
        this.doChangeStatus(row, status, value)
      }).catch(() => {})
    },
    doChangeStatus(row, status, reason) {
      if (status === 'LEAVE' && !reason) {
        this.$modal.msgError('请假必须填写理由')
        return
      }
      updateResultStatus({ id: row.id, status: status, reason: reason }).then(() => {
        this.$modal.msgSuccess('已更新')
        this.getDetail()
      })
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  margin: 24px 0 12px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
  font-size: 15px;
  font-weight: 600;
  line-height: 16px;
  color: #303133;
}

.section-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.add-row-btn {
  margin-top: 10px;
}

.action-bar {
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}
</style>
