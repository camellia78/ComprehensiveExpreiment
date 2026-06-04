<template>
  <div>
    <div class="toolbar"><h4>调寝处理</h4></div>
    <el-form :model="form" ref="formRef" label-width="100px" style="max-width:600px">
      <el-form-item label="选择学生">
        <el-select v-model="form.studentId" filterable @change="onStudentChange"><el-option v-for="c in checkins" :key="c.studentId" :label="getStudentName(c.studentId)" :value="c.studentId" /></el-select>
      </el-form-item>
      <el-form-item label="当前床位"><el-input :value="currentBedInfo" disabled /></el-form-item>
      <el-form-item label="目标楼栋">
        <el-select v-model="form.buildingId" @change="onTargetBuildingChange"><el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" /></el-select>
      </el-form-item>
      <el-form-item label="目标床位">
        <el-select v-model="form.newBedId" placeholder="选择空闲床位" :disabled="!form.buildingId"><el-option v-for="bed in freeBeds" :key="bed.id" :label="getRoomNo(bed.roomId) + ' - 床位' + bed.bedNo" :value="bed.id" /></el-select>
      </el-form-item>
      <el-form-item label="调寝原因"><el-input v-model="form.reason" type="textarea" /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleSubmit">确认调寝</el-button></el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBuildings, getFreeBeds, getRooms } from '../../api/dorm'
import { getUsers, getCheckins, doTransfer } from '../../api/accom'
import { ElMessage } from 'element-plus'

const buildings = ref([]); const students = ref([]); const checkins = ref([]); const freeBeds = ref([]); const rooms = ref([])
const form = reactive({ studentId: null, buildingId: null, newBedId: null, newRoomId: null, reason: '' })
const currentBedInfo = ref('')

const getStudentName = (id) => { const s = students.value.find(i => i.id === id); return s && s.realName }
const getRoomNo = (id) => { const r = rooms.value.find(i => i.id === id); return r && r.roomNo }
const getBuildingName = (id) => { const b = buildings.value.find(i => i.id === id); return b && b.name }

const fetchData = async () => {
  buildings.value = await getBuildings()
  const userPage = await getUsers({ page: 1, size: 100 }); students.value = userPage.records
  checkins.value = await getCheckins()
  const roomPage = await getRooms({ page: 1, size: 200 }); rooms.value = roomPage.records
}

const onStudentChange = () => {
  const c = checkins.value.find(i => i.studentId === form.studentId)
  if (c) currentBedInfo.value = getBuildingName(c.buildingId) + ' ' + getRoomNo(c.roomId) + ' 床位' + c.bedId
  else currentBedInfo.value = ''
}

const onTargetBuildingChange = async () => { form.newBedId = null; form.newRoomId = null; freeBeds.value = await getFreeBeds(form.buildingId) }

const handleSubmit = async () => {
  if (!form.studentId || !form.newBedId) { ElMessage.warning('请完整填写'); return }
  const bed = freeBeds.value.find(b => b.id === form.newBedId); form.newRoomId = bed.roomId
  await doTransfer(form); ElMessage.success('调寝成功'); fetchData()
}

onMounted(fetchData)
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>