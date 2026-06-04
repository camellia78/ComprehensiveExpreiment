<template>
  <div>
    <div class="toolbar"><h4>退宿登记</h4></div>
    <el-form :model="form" ref="formRef" label-width="100px" style="max-width:600px">
      <el-form-item label="选择学生">
        <el-select v-model="form.studentId" filterable @change="onStudentChange"><el-option v-for="c in checkins" :key="c.studentId" :label="getStudentName(c.studentId)" :value="c.studentId" /></el-select>
      </el-form-item>
      <el-form-item label="当前信息"><el-input :value="currentInfo" disabled /></el-form-item>
      <el-form-item label="退宿原因"><el-input v-model="form.reason" type="textarea" /></el-form-item>
      <el-form-item><el-button type="danger" @click="handleSubmit">确认退宿</el-button></el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBuildings, getRooms } from '../../api/dorm'
import { getUsers, getCheckins, doCheckout } from '../../api/accom'
import { ElMessage, ElMessageBox } from 'element-plus'

const buildings = ref([]); const students = ref([]); const checkins = ref([]); const rooms = ref([])
const form = reactive({ studentId: null, reason: '' })
const currentInfo = ref('')

const getStudentName = (id) => { const s = students.value.find(i => i.id === id); return s && s.realName }
const getBuildingName = (id) => { const b = buildings.value.find(i => i.id === id); return b && b.name }
const getRoomNo = (id) => { const r = rooms.value.find(i => i.id === id); return r && r.roomNo }

const fetchData = async () => {
  buildings.value = await getBuildings()
  const userPage = await getUsers({ page: 1, size: 100 }); students.value = userPage.records
  checkins.value = await getCheckins()
  const roomPage = await getRooms({ page: 1, size: 200 }); rooms.value = roomPage.records
}

const onStudentChange = () => {
  const c = checkins.value.find(i => i.studentId === form.studentId)
  if (c) currentInfo.value = getBuildingName(c.buildingId) + ' ' + getRoomNo(c.roomId) + ' 床位' + c.bedId
  else currentInfo.value = ''
}

const handleSubmit = async () => {
  if (!form.studentId) { ElMessage.warning('请选择学生'); return }
  await ElMessageBox.confirm('确定退宿？', '确认', { type: 'warning' })
  await doCheckout(form); ElMessage.success('退宿成功'); fetchData()
}

onMounted(fetchData)
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>
