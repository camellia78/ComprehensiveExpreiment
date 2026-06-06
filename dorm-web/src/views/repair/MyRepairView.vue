<template>
  <div>
    <div class="toolbar"><el-button type="primary" @click="showDialog()">提交报修</el-button></div>

    <el-table :data="list.records" border stripe v-loading="loading">
      <el-table-column prop="id" label="编号" width="170" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="repairType" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.repairType)" size="small">{{ row.repairType || '其他' }}</el-tag>
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
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link size="small" @click="showHistory(row)">进度</el-button>
          <el-button v-if="row.status === 0" link size="small" type="danger" @click="handleCancel(row.id)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="list.total"
      layout="prev, pager, next, total" @current-change="fetchList"
      style="margin-top:16px;justify-content:flex-end" />

    <!-- 提交弹窗 -->
    <el-dialog title="提交报修" v-model="dialogVisible" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="寝室" prop="roomId">
          <el-select v-model="form.roomId" :disabled="!form.buildingId" placeholder="请选择">
            <el-option v-for="r in filteredRooms" :key="r.id" :label="r.roomNo" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="报修类型" prop="repairType">
          <el-select v-model="form.repairType" placeholder="请选择">
            <el-option value="水电" label="水电" /><el-option value="家具" label="家具" />
            <el-option value="门窗" label="门窗" /><el-option value="网络" label="网络" />
            <el-option value="其他" label="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 处理进度弹窗 -->
    <el-dialog title="处理进度" v-model="historyVisible" width="520px">
      <div class="dialog-info" style="margin-bottom:16px">
        <p><b>编号：</b>{{ historyTarget.id }}</p>
        <p><b>描述：</b>{{ historyTarget.description }}</p>
        <p><b>类型：</b>{{ historyTarget.repairType || '其他' }}</p>
      </div>
      <el-timeline v-if="historyLog.length">
        <el-timeline-item v-for="h in historyLog" :key="h.id"
          :timestamp="h.processTime" placement="top"
          :type="h.action === '维修完成' ? 'success' : h.action === '无法修复' ? 'danger' : 'primary'">
          <p><b>{{ h.action }}</b></p>
          <p v-if="h.remark" style="color:#909399;margin-top:4px">{{ h.remark }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无处理记录，请耐心等待" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { submitRepair, getMyRepairs, cancelRepair, getProcessHistory } from '../../api/repair'
import { getBuildings, getRooms } from '../../api/dorm'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref({ records: [], total: 0 }); const loading = ref(false); const page = ref(1); const size = ref(10)
const buildings = ref([]); const allRooms = ref([]); const dialogVisible = ref(false); const formRef = ref()
const form = reactive({ buildingId: null, roomId: null, description: '', repairType: '其他' })
const filteredRooms = computed(() => allRooms.value.filter(r => r.buildingId === form.buildingId))
const historyVisible = ref(false); const historyTarget = ref({}); const historyLog = ref([])

const typeTag = (t) => ({ '水电': 'warning', '家具': '', '门窗': 'info', '网络': 'success', '其他': '' }[t] || '')

const fetchList = async () => { loading.value = true; list.value = await getMyRepairs({ page: page.value, size: size.value }); loading.value = false }
const fetchMeta = async () => { buildings.value = await getBuildings(); const rp = await getRooms({ page: 1, size: 200 }); allRooms.value = rp.records }

const showDialog = () => { Object.assign(form, { buildingId: null, roomId: null, description: '', repairType: '其他' }); dialogVisible.value = true }

const handleSubmit = async () => {
  if (!form.buildingId || !form.roomId || !form.description) { ElMessage.warning('请完整填写'); return }
  await submitRepair(form)
  ElMessage.success('提交成功')
  dialogVisible.value = false
  fetchList()
}

const showHistory = async (row) => {
  historyTarget.value = row
  historyVisible.value = true
  historyLog.value = await getProcessHistory(row.id)
}

const handleCancel = async (id) => {
  await ElMessageBox.confirm('确定取消该报修？', '确认', { type: 'warning' })
  await cancelRepair(id)
  ElMessage.success('已取消')
  fetchList()
}

onMounted(() => { fetchList(); fetchMeta() })
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.dialog-info p { margin: 4px 0; font-size: 14px; color: #606266; }
</style>
