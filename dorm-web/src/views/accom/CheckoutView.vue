<template>
  <div>
    <div class="toolbar"><h4>退宿登记</h4></div>
    <el-form :model="form" ref="formRef" label-width="100px" style="max-width:600px">
      <el-form-item label="选择学生">
        <el-select v-model="form.studentId" filterable @change="onStudentChange" style="width:100%">
          <el-option v-for="c in checkins" :key="c.studentId" :label="c.studentName + ' - ' + c.buildingName + ' ' + c.roomNo + ' 床位' + c.bedNo" :value="c.studentId" />
        </el-select>
      </el-form-item>
      <el-form-item label="当前位置">
        <el-input :value="currentPos" disabled />
      </el-form-item>
      <el-form-item label="退宿原因">
        <el-input v-model="form.reason" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item><el-button type="danger" @click="handleSubmit">确认退宿</el-button></el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCheckins, doCheckout } from '../../api/accom'
import { ElMessage, ElMessageBox } from 'element-plus'

const checkins = ref([])
const form = reactive({ studentId: null, reason: '' })
const currentPos = ref('')

const fetchData = async () => {
  const res = await getCheckins({ page: 1, size: 100, status: 1 })
  checkins.value = res.records || []
}

const onStudentChange = () => {
  const c = checkins.value.find(i => i.studentId === form.studentId)
  currentPos.value = c ? c.buildingName + ' ' + c.roomNo + ' 床位' + c.bedNo : ''
}

const handleSubmit = async () => {
  if (!form.studentId) { ElMessage.warning('请选择学生'); return }
  await ElMessageBox.confirm('确定退宿？', '确认', { type: 'warning' })
  await doCheckout({ studentId: form.studentId, reason: form.reason })
  ElMessage.success('退宿成功')
  form.studentId = null; form.reason = ''; currentPos.value = ''
  fetchData()
}

onMounted(fetchData)
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>