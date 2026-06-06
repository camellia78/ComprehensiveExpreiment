<template>
  <div>
    <div class="toolbar"><h4>调寝处理</h4></div>
    <el-form :model="form" ref="formRef" label-width="100px" style="max-width:600px">
      <el-form-item label="选择学生">
        <el-select v-model="form.studentId" filterable @change="onStudentChange" style="width:100%">
          <el-option v-for="c in checkins" :key="c.studentId" :label="c.studentName + ' - ' + c.buildingName + ' ' + c.roomNo + ' 床位' + c.bedNo" :value="c.studentId" />
        </el-select>
      </el-form-item>
      <el-form-item label="当前位置">
        <el-input :value="currentPos" disabled />
      </el-form-item>
      <el-form-item label="目标楼栋">
        <el-select v-model="form.buildingId" @change="onTargetBuildingChange" placeholder="选择楼栋" style="width:100%">
          <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标床位">
        <el-select v-model="form.newBedId" placeholder="选择空闲床位" :disabled="!form.buildingId" style="width:100%">
          <el-option v-for="bed in freeBeds" :key="bed.id" :label="bed.roomNo + ' - 床位' + bed.bedNo" :value="bed.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="调寝原因">
        <el-input v-model="form.reason" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item><el-button type="primary" @click="handleSubmit">确认调寝</el-button></el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBuildings, getFreeBeds, getRooms } from '../../api/dorm'
import { getCheckins, doTransfer } from '../../api/accom'
import { ElMessage } from 'element-plus'

const buildings = ref([]); const checkins = ref([]); const freeBeds = ref([]); const rooms = ref([])
const form = reactive({ studentId: null, buildingId: null, newBedId: null, newRoomId: null, reason: '' })
const currentPos = ref('')

const fetchData = async () => {
  buildings.value = await getBuildings()
  const res = await getCheckins({ page: 1, size: 100, status: 1 })
  checkins.value = res.records || []
  const roomPage = await getRooms({ page: 1, size: 200 })
  rooms.value = roomPage.records || []
}

const onStudentChange = () => {
  const c = checkins.value.find(i => i.studentId === form.studentId)
  currentPos.value = c ? c.buildingName + ' ' + c.roomNo + ' 床位' + c.bedNo : ''
}

const onTargetBuildingChange = async () => {
  form.newBedId = null; form.newRoomId = null
  const beds = await getFreeBeds(form.buildingId)
  freeBeds.value = beds ? beds.map(b => {
    const r = rooms.value.find(rr => rr.id === b.roomId)
    return { ...b, roomNo: r ? r.roomNo : '' }
  }) : []
}

const handleSubmit = async () => {
  if (!form.studentId || !form.newBedId) { ElMessage.warning('请完整填写'); return }
  const bed = freeBeds.value.find(b => b.id === form.newBedId)
  if (!bed) return
  form.newRoomId = bed.roomId
  await doTransfer({
    studentId: form.studentId,
    newBedId: form.newBedId,
    newRoomId: form.newRoomId,
    reason: form.reason
  })
  ElMessage.success('调寝成功')
  form.studentId = null; form.buildingId = null; form.newBedId = null; form.reason = ''
  currentPos.value = ''; freeBeds.value = []
  fetchData()
}

onMounted(fetchData)
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>