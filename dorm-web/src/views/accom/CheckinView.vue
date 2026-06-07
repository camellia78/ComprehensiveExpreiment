<template>
  <div>
    <div class="toolbar"><h4>入住登记</h4></div>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width:600px">
      <el-form-item label="选择学生" prop="studentId">
        <el-select v-model="form.studentId" filterable placeholder="搜索学号或姓名"><el-option v-for="s in students" :key="s.id" :label="s.studentNo + ' ' + s.realName" :value="s.id" /></el-select>
      </el-form-item>
      <el-form-item label="选择楼栋" prop="buildingId">
        <el-select v-model="form.buildingId" @change="onBuildingChange" placeholder="先选楼栋"><el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" /></el-select>
      </el-form-item>
      <el-form-item label="选择寝室" prop="roomId">
        <el-select v-model="form.roomId" @change="onRoomChange" placeholder="选择寝室" :disabled="!form.buildingId"><el-option v-for="r in filteredRooms" :key="r.id" :label="r.roomNo" :value="r.id" /></el-select>
      </el-form-item>
      <el-form-item label="空闲床位" prop="bedId">
        <el-select v-model="form.bedId" placeholder="选择床位" :disabled="!form.roomId"><el-option v-for="bed in freeBeds" :key="bed.id" :label="'床位' + bed.bedNo" :value="bed.id" /></el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="handleSubmit">确认入住</el-button></el-form-item>
    </el-form>
    <el-divider />
    <h4 style="margin-bottom:12px">当前入住记录</h4>
    <el-table :data="checkins" border stripe>
      <el-table-column label="学生" width="120"><template #default="{ row }">{{ getStudentName(row.studentId) }}</template></el-table-column>
      <el-table-column label="楼栋" width="120"><template #default="{ row }">{{ getBuildingName(row.buildingId) }}</template></el-table-column>
      <el-table-column label="房间" width="80"><template #default="{ row }">{{ getRoomNo(row.roomId) }}</template></el-table-column>
      <el-table-column label="床位" width="80"><template #default="{ row }">{{ getBedNo(row.bedId) }}</template></el-table-column>
      <el-table-column label="入住时间" prop="checkinTime" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getBuildings, getFreeBeds, getRooms, getRoomBeds } from '../../api/dorm'
import { getUsers, getCheckins, doCheckin } from '../../api/accom'
import { ElMessage } from 'element-plus'

const allStudents = ref([])
const buildings = ref([]); const students = ref([]); const freeBeds = ref([]); const checkins = ref([]); const rooms = ref([]); const allBeds = ref([])
const formRef = ref()
const form = reactive({ studentId: null, buildingId: null, roomId: null, bedId: null })
const rules = { studentId: [{ required: true }], buildingId: [{ required: true }], roomId: [{ required: true }], bedId: [{ required: true }] }

const filteredRooms = computed(() => rooms.value.filter(r => r.buildingId === form.buildingId))
const getBuildingName = (id) => { const b = buildings.value.find(i => i.id === id); return b && b.name }
const getRoomNo = (id) => { const r = rooms.value.find(i => i.id === id); return r && r.roomNo }
const getStudentName = (id) => { const s = allStudents.value.find(i => i.id === id); return s && s.realName }
const getBedNo = (id) => { const b = allBeds.value.find(i => i.id === id); return b && b.bedNo }

const fetchData = async () => {
  buildings.value = await getBuildings()
  checkins.value = await getCheckins(); const ids = new Set(checkins.value.map(c => c.studentId))
  const userPage = await getUsers({ page: 1, size: 100 }); allStudents.value = userPage.records; students.value = userPage.records.filter(u => u.role === 1 && !ids.has(u.id))
  const roomPage = await getRooms({ page: 1, size: 200 }); rooms.value = roomPage.records
  const bedList = []
  for (const r of rooms.value) {
    try { const beds = await getRoomBeds(r.id); bedList.push(...beds) } catch (_) {}
  }
  allBeds.value = bedList
}

const onBuildingChange = () => { form.roomId = null; form.bedId = null; freeBeds.value = [] }
const onRoomChange = async () => { form.bedId = null; const beds = await getFreeBeds(form.buildingId); freeBeds.value = beds.filter(b => b.roomId === form.roomId) }

const handleSubmit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  await doCheckin(form); ElMessage.success('入住成功'); fetchData()
}

onMounted(fetchData)
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>