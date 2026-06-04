<template>
  <div>
    <div class="toolbar"><el-button type="primary" @click="showDialog()">提交报修</el-button></div>
    <el-table :data="list.records" border stripe v-loading="loading">
      <el-table-column prop="id" label="编号" width="180" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="['warning','primary','success','info'][row.status]">{{ ['待处理','处理中','已完成','已取消'][row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }"><el-button v-if="row.status === 0" size="small" type="warning" @click="handleCancel(row.id)">取消</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="list.total" layout="prev, pager, next, total" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" />
    <el-dialog title="提交报修" v-model="dialogVisible" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="楼栋" prop="buildingId"><el-select v-model="form.buildingId"><el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" /></el-select></el-form-item>
        <el-form-item label="寝室" prop="roomId"><el-select v-model="form.roomId" :disabled="!form.buildingId"><el-option v-for="r in filteredRooms" :key="r.id" :label="r.roomNo" :value="r.id" /></el-select></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { submitRepair, getMyRepairs, cancelRepair } from '../../api/repair'
import { getBuildings, getRooms } from '../../api/dorm'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref({ records: [], total: 0 }); const loading = ref(false); const page = ref(1); const size = ref(10)
const buildings = ref([]); const allRooms = ref([]); const dialogVisible = ref(false); const formRef = ref()
const form = reactive({ buildingId: null, roomId: null, description: '' })
const filteredRooms = computed(() => allRooms.value.filter(r => r.buildingId === form.buildingId))

const fetchList = async () => { loading.value = true; list.value = await getMyRepairs({ page: page.value, size: size.value }); loading.value = false }
const fetchMeta = async () => { buildings.value = await getBuildings(); const rp = await getRooms({ page: 1, size: 200 }); allRooms.value = rp.records }

const showDialog = () => { Object.assign(form, { buildingId: null, roomId: null, description: '' }); dialogVisible.value = true }

const handleSubmit = async () => {
  if (!form.buildingId || !form.roomId || !form.description) { ElMessage.warning('请完整填写'); return }
  await submitRepair(form); ElMessage.success('提交成功'); dialogVisible.value = false; fetchList()
}

const handleCancel = async (id) => { await ElMessageBox.confirm('确定取消？', '确认', { type: 'warning' }); await cancelRepair(id); ElMessage.success('已取消'); fetchList() }

onMounted(() => { fetchList(); fetchMeta() })
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>
