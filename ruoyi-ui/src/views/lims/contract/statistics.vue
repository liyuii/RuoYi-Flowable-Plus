<template>
  <div v-loading="loading" class="app-container contract-statistics">
    <el-row :gutter="12">
      <el-col v-for="item in cards" :key="item.key" :xs="12" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="chart-row">
      <el-col :xs="24" :sm="24" :md="12">
        <div class="chart-panel">
          <div class="chart-title">合同状态分布</div>
          <div ref="statusChart" class="chart-box" />
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <div class="chart-panel">
          <div class="chart-title">合同类型金额分布</div>
          <div ref="typeChart" class="chart-box" />
        </div>
      </el-col>
    </el-row>

    <div class="chart-panel trend-panel">
      <div class="chart-title">月度合同趋势</div>
      <div ref="monthChart" class="chart-box trend-box" />
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
require('echarts/theme/macarons')
import { getContractStatistics } from '@/api/lims/contract'

const statusMap = {
  '0': { name: '草稿', color: '#909399' },
  '1': { name: '审批中', color: '#409eff' },
  '3': { name: '已驳回', color: '#f56c6c' },
  '9': { name: '已通过', color: '#67c23a' }
}

const typeMap = {
  '1': '采购合同',
  '2': '销售合同',
  '3': '服务合同'
}

export default {
  name: 'ContractStatistics',
  data() {
    return {
      loading: false,
      statistics: { summary: {}, statusList: [], typeList: [], monthlyList: [] },
      charts: {}
    }
  },
  computed: {
    cards() {
      const summary = this.statistics.summary || {}
      return [
        { key: 'totalCount', label: '合同总数', value: this.formatCount(summary.totalCount) },
        { key: 'totalAmount', label: '合同总金额', value: this.formatAmount(summary.totalAmount) },
        { key: 'approvedCount', label: '已通过合同数', value: this.formatCount(summary.approvedCount) },
        { key: 'approvedAmount', label: '已通过合同金额', value: this.formatAmount(summary.approvedAmount) }
      ]
    }
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
    this.loadStatistics()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    Object.values(this.charts).forEach(chart => {
      if (chart) {
        chart.dispose()
      }
    })
    this.charts = {}
  },
  methods: {
    loadStatistics() {
      this.loading = true
      getContractStatistics().then(res => {
        this.statistics = res.data || { summary: {}, statusList: [], typeList: [], monthlyList: [] }
        this.$nextTick(() => {
          this.initCharts()
        })
      }).finally(() => {
        this.loading = false
      })
    },
    initCharts() {
      this.initStatusChart()
      this.initTypeChart()
      this.initMonthChart()
    },
    initStatusChart() {
      const chart = this.getChart('statusChart')
      if (!chart) {
        return
      }
      const map = {}
      ;(this.statistics.statusList || []).forEach(item => {
        map[item.status] = item.count
      })
      const data = Object.keys(statusMap).map(status => {
        const info = statusMap[status]
        return {
          name: info.name,
          value: Number(map[status] || 0),
          itemStyle: { color: info.color }
        }
      })
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0 },
        series: [{
          name: '合同状态',
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          label: { show: false },
          emphasis: { label: { show: true, fontWeight: 'bold' }},
          data: data
        }]
      }, true)
    },
    initTypeChart() {
      const chart = this.getChart('typeChart')
      if (!chart) {
        return
      }
      const rows = []
      ;(this.statistics.typeList || []).forEach(item => {
        rows.push({
          name: typeMap[item.contractType] || '未知类型',
          count: item.count,
          amount: item.amount
        })
      })
      const names = rows.map(item => item.name)
      const amounts = rows.map(item => item.amount || 0)
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter(params) {
            const index = params[0].dataIndex
            return rows[index].name + '<br/>数量: ' + rows[index].count + '<br/>金额: ' + formatAmountValue(rows[index].amount)
          }
        },
        grid: { left: 10, right: 30, top: 20, bottom: 20, containLabel: true },
        xAxis: { type: 'value', name: '金额' },
        yAxis: { type: 'category', data: names },
        series: [{
          name: '合同金额',
          type: 'bar',
          barWidth: 18,
          data: amounts,
          itemStyle: { color: '#409eff' }
        }]
      }, true)
    },
    initMonthChart() {
      const chart = this.getChart('monthChart')
      if (!chart) {
        return
      }
      const months = this.lastMonths()
      const map = {}
      ;(this.statistics.monthlyList || []).forEach(item => {
        map[item.month] = item
      })
      const counts = months.map(month => Number(map[month] ? map[month].count : 0))
      const amounts = months.map(month => Number(map[month] ? map[month].amount : 0))
      chart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['合同数量', '合同金额'] },
        grid: { left: 10, right: 10, top: 40, bottom: 20, containLabel: true },
        xAxis: { type: 'category', data: months },
        yAxis: [
          { type: 'value', name: '数量' },
          { type: 'value', name: '金额' }
        ],
        series: [
          { name: '合同数量', type: 'bar', barWidth: 16, data: counts, itemStyle: { color: '#409eff' }},
          { name: '合同金额', type: 'line', yAxisIndex: 1, smooth: true, data: amounts, itemStyle: { color: '#f56c6c' }}
        ]
      }, true)
    },
    getChart(refName) {
      const el = this.$refs[refName]
      if (!el) {
        return null
      }
      if (this.charts[refName]) {
        this.charts[refName].dispose()
      }
      const chart = echarts.init(el, 'macarons')
      this.charts[refName] = chart
      return chart
    },
    handleResize() {
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.resize()
        }
      })
    },
    lastMonths() {
      const result = []
      const now = new Date()
      for (let i = 11; i >= 0; i--) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
        const month = String(date.getMonth() + 1).padStart(2, '0')
        result.push(date.getFullYear() + '-' + month)
      }
      return result
    },
    formatCount(value) {
      return value === null || value === undefined ? '0' : String(value)
    },
    formatAmount(value) {
      return 'CNY ' + formatAmountValue(value)
    }
  }
}

function formatAmountValue(value) {
  if (value === null || value === undefined) {
    return '0.00'
  }
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}
</script>

<style scoped>
.contract-statistics {
  min-height: 520px;
}
.stat-card {
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  background: #fff;
  padding: 16px;
  margin-bottom: 12px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 22px;
  font-weight: 600;
  word-break: break-all;
}
.chart-row {
  margin-top: 4px;
}
.chart-panel {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 12px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
}
.chart-box {
  height: 320px;
}
.trend-panel {
  margin-top: 4px;
}
.trend-box {
  height: 360px;
}
</style>
