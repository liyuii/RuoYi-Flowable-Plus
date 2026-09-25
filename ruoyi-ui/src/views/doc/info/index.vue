<template>
  <div class="app-container">
    <!-- 查询条件 -->
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true" size="small" label-width="80px">
      <el-form-item label="文档名称" prop="docName">
        <el-input
          v-model="queryParams.docName"
          placeholder="请输入文档名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
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
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="docList">
      <el-table-column label="首页" align="center" width="90">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.coverUrl"
            :src="scope.row.coverUrl"
            :preview-src-list="[scope.row.coverUrl]"
            fit="cover"
            style="width: 46px; height: 62px; border: 1px solid #ebeef5"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="文档名称" align="left" prop="docName" min-width="200" show-overflow-tooltip />
      <el-table-column label="项目名称" align="left" prop="projectName" min-width="180" show-overflow-tooltip />
      <el-table-column label="所在省份" align="center" width="130">
        <template slot-scope="scope">{{ provinceName(scope.row.provinceCode) }}</template>
      </el-table-column>
      <el-table-column label="金额（元）" align="right" width="140">
        <template slot-scope="scope">{{ formatAmount(scope.row.amount) }}</template>
      </el-table-column>
      <el-table-column label="页数" align="center" prop="pageCount" width="80" />
      <el-table-column label="入库时间" align="center" prop="createTime" width="170" />
      <el-table-column label="操作" align="center" width="220">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-view" size="mini" @click="handleDetail(scope.row)">详情</el-button>
          <!-- <el-button type="text" icon="el-icon-download" size="mini" @click="handleDownload(scope.row)">下载</el-button> -->
          <!-- <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(scope.row)">删除</el-button> -->
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

    <!-- 详情：文档信息 + 页面图片逐页预览 -->
    <el-dialog :title="detailTitle" :visible.sync="detailVisible" width="900px" append-to-body>
      <div v-loading="detailLoading">
        <el-descriptions v-if="detail.doc" :column="2" border size="small" class="doc-info">
          <el-descriptions-item label="文档名称">{{ detail.doc.docName }}</el-descriptions-item>
          <el-descriptions-item label="项目名称">{{ detail.doc.projectName }}</el-descriptions-item>
          <el-descriptions-item label="所在省份">{{ provinceName(detail.doc.provinceCode) }}</el-descriptions-item>
          <el-descriptions-item label="项目金额">{{ formatAmount(detail.doc.amount) }} 元</el-descriptions-item>
          <el-descriptions-item label="页数">{{ detail.doc.pageCount }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ detail.doc.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="page-viewer">
          <el-image
            v-if="currentPage"
            :src="currentPage.imageUrl"
            :preview-src-list="pageUrls"
            :initial-index="pageIndex"
            fit="contain"
            class="page-image"
          />
          <el-empty v-else description="暂无页面图片" />
        </div>

        <div v-if="pages.length" class="page-bar">
          <el-button size="mini" icon="el-icon-arrow-left" :disabled="pageIndex <= 0" @click="pageIndex--">上一页</el-button>
          <span class="page-tip">第 {{ pageIndex + 1 }} / {{ pages.length }} 页</span>
          <el-button
            size="mini"
            :disabled="pageIndex >= pages.length - 1"
            @click="pageIndex++"
          >下一页<i class="el-icon-arrow-right el-icon--right" /></el-button>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="detailVisible = false">关 闭</el-button>
        <el-button type="primary" size="mini" icon="el-icon-download" @click="handleDownload(detail.doc)">下载文档</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDocInfo, getDocInfo, delDocInfo, downloadDocInfo } from '@/api/doc/info'
import { PROVINCE_OPTIONS, getProvinceName } from '@/utils/province'

export default {
  name: 'DocInfo',
  data() {
    return {
      loading: false,
      showSearch: true,
      docList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, docName: null, projectName: null, provinceCode: null },
      provinceOptions: PROVINCE_OPTIONS,
      detailVisible: false,
      detailLoading: false,
      detail: { doc: null, pages: [] },
      pageIndex: 0
    }
  },
  computed: {
    pages() {
      return this.detail.pages || []
    },
    currentPage() {
      return this.pages[this.pageIndex] || null
    },
    pageUrls() {
      return this.pages.map(p => p.imageUrl)
    },
    detailTitle() {
      return this.detail.doc ? '文档详情 - ' + this.detail.doc.docName : '文档详情'
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDocInfo(this.queryParams).then(res => {
        this.docList = res.rows
        this.total = res.total
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
      this.queryParams.docName = null
      this.queryParams.projectName = null
      this.queryParams.provinceCode = null
      this.handleQuery()
    },
    provinceName(code) {
      return getProvinceName(code)
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
    handleDetail(row) {
      this.pageIndex = 0
      this.detail = { doc: null, pages: [] }
      this.detailVisible = true
      this.detailLoading = true
      getDocInfo(row.id).then(res => {
        this.detail = res.data || { doc: null, pages: [] }
        this.detailLoading = false
      }).catch(() => {
        this.detailLoading = false
      })
    },
    async handleDownload(row) {
      if (!row || !row.id) {
        return
      }
      try {
        const data = await downloadDocInfo(row.id)
        if (data.type && data.type.indexOf('application/json') !== -1) {
          this.$modal.msgError('下载失败')
          return
        }
        const fileName = row.docName || '文档.docx'
        const url = URL.createObjectURL(new Blob([data], { type: 'application/octet-stream' }))
        const link = document.createElement('a')
        link.href = url
        link.download = fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
      } catch (e) {
        this.$modal.msgError('下载失败')
      }
    },
    handleDelete(row) {
      const name = row.docName || ''
      this.$modal.confirm('确认删除文档「' + name + '」？删除后文档库不再展示（不影响源系统的文件）。').then(() => {
        return delDocInfo(row.id)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.doc-info {
  margin-bottom: 12px;
}
.page-viewer {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6f8;
  border: 1px solid #ebeef5;
  padding: 8px;
}
.page-image {
  max-height: 620px;
}
.page-bar {
  margin-top: 10px;
  text-align: center;
}
.page-tip {
  margin: 0 12px;
  font-size: 13px;
  color: #606266;
}
</style>
