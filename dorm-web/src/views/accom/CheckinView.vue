<template>
  <div>
    <div class="toolbar"><h4>入住登记</h4></div>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width:600px">
      <el-form-item label="选择学生" prop="studentId">
        <el-select v-model="form.studentId" filterable placeholder="搜索学号或姓名" style="width:100%">
          <el-option v-for="s in students" :key="s.id" :label="(s.studentNo||'管理员') + ' ' + s.realName" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="选择楼栋" prop="buildingId">
        <el-select v-model="form.buildingId" @change="onBuildingChange" placeholder="先选楼栋" style="width:100%">
          <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="选择寝室" prop="roomId">
        <el-select v-model="form.roomId" @change="onRoomChange" placeholder="选择寝室" :disabled="!form.buildingId" style="width:100%">
          <el-option v-for="r in filteredRooms" :key="r.id" :label="r.roomNo + ' (剩余' + (r.totalBeds - r.occupiedBeds) + '床)'" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="空闲床位" prop="bedId">
        <el-select v-model="form.bedId" placeholder="选择床位" :disabled="!form.roomId" style="width:100%">
          <el-option v-for="bed in freeBeds" :key="bed.id" :label="'床位 ' + bed.bedNo" :value="bed.id" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="handleSubmit">确认入住</el-button></el-form-item>
    </el-form>
    <el-divider />
    <h4 style="margin-bottom:12px">当前入住记录</h4>
    <el-table :data="checkins" border stripe v-loading="loading">
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="buildingName" label="楼栋" width="100" />
      <el-table-column prop="roomNo" label="房间" width="70" />
      <el-table-column prop="bedNo" label="床位" width="70" />
      <el-table-column prop="checkinTime" label="入住时间" width="170" />
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total"
      layout="prev, pager, next, total" @current-change="fetchCheckins"
      style="margin-top:12px;justify-content:flex-end" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getBuildings, getFreeBeds, getRooms } from '../../api/dorm'
import { getUsers } from '../../api/auth'
import { getCheckins, doCheckin } from '../../api/accom'
import { ElMessage } from 'element-plus'

const buildings = ref([]); const students = ref([]); const freeBeds = ref([])
const checkins = ref([]); const rooms = ref([]); const loading = ref(false)
const page = ref(1); const size = ref(10); const total = ref(0)
const formRef = ref()
const form = reactive({ studentId: null, buildingId: null, roomId: null, bedId: null })
const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  bedId: [{ required: true, message: '请选择床位', trigger: 'change' }]
}

const filteredRooms = computed(() => rooms.value.filter(r => r.buildingId === form.buildingId))

const fetchData = async () => {
  buildings.value = await getBuildings()
  const userPage = await getUsers({ page: 1, size: 100 })
  students.value = userPage.records ? userPage.records.filter(u => u.role === 1) : []
  const roomPage = await getRooms({ page: 1, size: 200 })
  rooms.value = roomPage.records || []
  fetchCheckins()
}

const fetchCheckins = async () => {
  loading.value = true
  const res = await getCheckins({ page: page.value, size: size.value, status: 1 })
  checkins.value = res.records || []
  total.value = parseInt(res.total) || 0
  loading.value = false
}

const onBuildingChange = () => { form.roomId = null; form.bedId = null; freeBeds.value = [] }
const onRoomChange = async () => {
  form.bedId = null
  const beds = await getFreeBeds(form.buildingId)
  freeBeds.value = beds ? beds.filter(b => b.roomId === form.roomId) : []
}

const handleSubmit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  await doCheckin({
    studentId: form.studentId,
    buildingId: form.buildingId,
    roomId: form.roomId,
    bedId: form.bedId
  })
  ElMessage.success('入住成功')
  form.studentId = null; form.buildingId = null; form.roomId = null; form.bedId = null
  fetchCheckins()
}

onMounted(fetchData)
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>