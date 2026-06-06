<template>
  <div>
    <div class="toolbar"><el-button type="primary" @click="showDialog()">新增楼栋</el-button></div>
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="name" label="楼栋名称" />
      <el-table-column prop="buildingNo" label="编号" />
      <el-table-column prop="floors" label="楼层数" />
      <el-table-column label="类型"><template #default="{ row }">{{ row.buildingType === 0 ? '男生' : '女生' }}</template></el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="isEdit ? '编辑楼栋' : '新增楼栋'" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编号" prop="buildingNo"><el-input v-model="form.buildingNo" /></el-form-item>
        <el-form-item label="楼层数" prop="floors"><el-input-number v-model="form.floors" :min="1" :max="20" /></el-form-item>
        <el-form-item label="类型" prop="buildingType"><el-select v-model="form.buildingType"><el-option :value="0" label="男生" /><el-option :value="1" label="女生" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确认</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBuildings, addBuilding, updateBuilding, deleteBuilding } from '../../api/dorm'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const form = reactive({ name: '', buildingNo: '', floors: 6, buildingType: 0 })
const rules = { name: [{ required: true, message: '请输入名称' }], buildingNo: [{ required: true, message: '请输入编号' }] }

const fetchList = async () => { loading.value = true; list.value = await getBuildings(); loading.value = false }

const showDialog = (row) => {
  if (row) { isEdit.value = true; editId.value = row.id; form.name = row.name; form.buildingNo = row.buildingNo; form.floors = row.floors; form.buildingType = row.buildingType }
  else { isEdit.value = false; editId.value = null; Object.assign(form, { name: '', buildingNo: '', floors: 6, buildingType: 0 }) }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  if (isEdit.value) { await updateBuilding(editId.value, form); ElMessage.success('更新成功') }
  else { await addBuilding(form); ElMessage.success('添加成功') }
  dialogVisible.value = false; fetchList()
}

const handleDelete = async (id) => { await ElMessageBox.confirm('确定删除？关联寝室将一并删除', '确认', { type: 'warning' }); await deleteBuilding(id); ElMessage.success('删除成功'); fetchList() }

onMounted(fetchList)
</script>

<style scoped>.toolbar { margin-bottom: 16px; }</style>
