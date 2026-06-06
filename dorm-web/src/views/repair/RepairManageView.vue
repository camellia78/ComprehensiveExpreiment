<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width:140px;margin-right:8px" @change="fetchList">
        <el-option :value="0" label="待处理" /><el-option :value="1" label="处理中" />
        <el-option :value="2" label="已完成" /><el-option :value="3" label="已取消" />
      </el-select>
      <el-select v-model="query.repairType" placeholder="报修类型" clearable style="width:140px;margin-right:8px" @change="fetchList">
        <el-option value="水电" label="水电" /><el-option value="家具" label="家具" />
        <el-option value="门窗" label="门窗" /><el-option value="网络" label="网络" />
        <el-option value="其他" label="其他" />
      </el-select>
    </div>

    <el-table :data="list.records" border stripe v-loading="loading">
      <el-table-column prop="id" label="编号" width="170" />
      <el-table-column prop="studentName" label="学生" width="90" />
      <el-table-column label="位置" width="150">
        <template #default="{ row }">{{ row.buildingName }} {{ row.roomNo }}</template>
      </el-table-column>
      <el-table-column prop="repairType" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.repairType)" size="small">{{ row.repairType || '其他' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="urgency" label="紧急" width="70" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.urgency === 1" type="danger" size="small">紧急</el-tag>
          <span v-else class="text-muted">普通</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="95">
        <template #default="{ row }">
          <el-tag :type="['warning','primary','success','info'][row.status]" size="small">
            {{ ['待处理','处理中','已完成','已取消'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="165" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="showHistory(row)">记录</el-button>
          <el-button v-if="row.status !== 2 && row.status !== 3" link size="small" type="primary" @click="showProcess(row)">处置</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="query.page" :page-size="query.size" :total="list.total"
      layout="prev, pager, next, total" @current-change="fetchList"
      style="margin-top:16px;justify-content:flex-end" />

    <!-- 处置弹窗 -->
    <el-dialog title="工单处置" v-model="dialogVisible" width="480px">
      <div class="dialog-info">
        <p><b>编号：</b>{{ processTarget.id }}</p>
        <p><b>学生：</b>{{ processTarget.studentName }}</p>
        <p><b>描述：</b>{{ processTarget.description }}</p>
        <p><b>类型：</b>{{ processTarget.repairType || '其他' }}</p>
      </div>
      <el-form :model="form" ref="formRef" label-width="80px" style="margin-top:16px">
        <el-form-item label="操作">
          <el-select v-model="form.action" @change="onActionChange">
            <el-option label="接单处理" value="接单处理" />
            <el-option label="维修完成" value="维修完成" />
            <el-option label="无法修复" value="无法修复" />
          </el-select>
        </el-form-item>
        <el-form-item label="最新状态">
          <el-select v-model="form.status">
            <el-option :value="1" label="处理中" />
            <el-option :value="2" label="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="处理说明/原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认处置</el-button>
      </template>
    </el-dialog>

    <!-- 处理记录弹窗 -->
    <el-dialog title="处理记录" v-model="historyVisible" width="550px">
      <div class="dialog-info" style="margin-bottom:16px">
        <p><b>编号：</b>{{ historyTarget.id }}</p>
        <p><b>描述：</b>{{ historyTarget.description }}</p>
      </div>
      <el-timeline v-if="history.length">
        <el-timeline-item v-for="h in history" :key="h.id"
          :timestamp="h.processTime" placement="top"
          :type="h.action === '维修完成' ? 'success' : h.action === '无法修复' ? 'danger' : 'primary'">
          <p><b>{{ h.action }}</b></p>
          <p v-if="h.remark" style="color:#909399;margin-top:4px">{{ h.remark }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无处理记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllRepairs, processRepair, getProcessHistory } from '../../api/repair'
import { ElMessage } from 'element-plus'

const list = ref({ records: [], total: 0 }); const loading = ref(false)
const query = reactive({ page: 1, size: 10, status: null, repairType: null })
const dialogVisible = ref(false); const historyVisible = ref(false)
const processTarget = ref({}); const historyTarget = ref({}); const history = ref([])
const formRef = ref()
const form = reactive({ action: '接单处理', status: 1, remark: '' })

const typeTag = (t) => ({ '水电': 'warning', '家具': '', '门窗': 'info', '网络': 'success', '其他': '' }[t] || '')

const onActionChange = (val) => { form.status = (val === '维修完成' || val === '无法修复') ? 2 : 1 }

const fetchList = async () => {
  loading.value = true
  const params = { page: query.page, size: query.size }
  if (query.status !== null && query.status !== '') params.status = query.status
  list.value = await getAllRepairs(params)
  // 前端再按类型过滤（后端暂未实现该参数）
  if (query.repairType) {
    list.value.records = list.value.records.filter(r => r.repairType === query.repairType)
  }
  loading.value = false
}

const showProcess = (row) => {
  processTarget.value = row
  Object.assign(form, { action: '接单处理', status: 1, remark: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await processRepair(processTarget.value.id, { action: form.action, remark: form.remark, status: form.status })
  ElMessage.success('处置成功')
  dialogVisible.value = false
  fetchList()
}

const showHistory = async (row) => {
  historyTarget.value = row
  historyVisible.value = true
  history.value = await getProcessHistory(row.id)
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.toolbar { margin-bottom: 16px; display: flex; gap: 8px; }
.dialog-info p { margin: 4px 0; font-size: 14px; color: #606266; }
.text-muted { color: #c0c4cc; font-size: 13px; }
</style>
