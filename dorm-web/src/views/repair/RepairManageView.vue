<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width:160px;margin-right:12px" @change="fetchList">
        <el-option :value="0" label="待处理" /><el-option :value="1" label="处理中" /><el-option :value="2" label="已完成" />
      </el-select>
    </div>
    <el-table :data="list.records" border stripe v-loading="loading">
      <el-table-column prop="id" label="编号" width="180" />
      <el-table-column label="学生" width="100"><template #default="{ row }">{{ getStudentName(row.studentId) }}</template></el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="['warning','primary','success'][row.status]">{{ ['待处理','处理中','已完成'][row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }"><el-button v-if="row.status !== 2" size="small" type="primary" @click="showProcess(row)">处置</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" :page-size="query.size" :total="list.total" layout="prev, pager, next, total" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" />
    <el-dialog title="工单处置" v-model="dialogVisible" width="500px">
      <el-form :model="form" ref="formRef" label-width="80px">
        <el-form-item label="操作"><el-select v-model="form.action"><el-option label="接单处理" value="接单处理" /><el-option label="维修完成" value="维修完成" /><el-option label="无法修复" value="无法修复" /></el-select></el-form-item>
        <el-form-item label="最终状态"><el-select v-model="form.status"><el-option :value="1" label="处理中" /><el-option :value="2" label="已完成" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确认处置</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllRepairs, processRepair } from '../../api/repair'
import { getUsers } from '../../api/accom'
import { ElMessage } from 'element-plus'

const list = ref({ records: [], total: 0 }); const loading = ref(false)
const query = reactive({ page: 1, size: 10, status: null }); const students = ref([])
const dialogVisible = ref(false); const currentId = ref(null)
const form = reactive({ action: '接单处理', status: 1, remark: '' })

const getStudentName = (id) => { const s = students.value.find(i => i.id === id); return s && s.realName }

const fetchList = async () => { loading.value = true; list.value = await getAllRepairs(query); loading.value = false }
const fetchStudents = async () => { const up = await getUsers({ page: 1, size: 200 }); students.value = up.records }

const showProcess = (row) => { currentId.value = row.id; Object.assign(form, { action: '接单处理', status: 1, remark: '' }); dialogVisible.value = true }

const handleSubmit = async () => { await processRepair(currentId.value, form); ElMessage.success('处置成功'); dialogVisible.value = false; fetchList() }

onMounted(() => { fetchList(); fetchStudents() })
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>
