<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.buildingId" placeholder="选择楼栋" clearable style="width:200px;margin-right:12px" @change="fetchList">
        <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
      <el-button type="primary" @click="showDialog()">新增寝室</el-button>
    </div>
    <el-table :data="list.records" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="roomNo" label="房间号" />
      <el-table-column label="楼栋" width="120"><template #default="{ row }">{{ getBuildingName(row.buildingId) }}</template></el-table-column>
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column label="床位" width="160"><template #default="{ row }">{{ row.occupiedBeds }} / {{ row.totalBeds }}</template></el-table-column>
      <el-table-column label="状态"><template #default="{ row }">{{ row.status === 0 ? '正常' : '维修中' }}</template></el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="showStudents(row)">查看</el-button>
          <el-button size="small" @click="showBeds(row)">床位</el-button>
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" :page-size="query.size" :total="list.total" layout="total, prev, pager, next, sizes" :page-sizes="[10,20,50,100]" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" />
    <el-dialog :title="isEdit ? '编辑寝室' : '新增寝室'" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="房间号" prop="roomNo"><el-input v-model="form.roomNo" /></el-form-item>
        <el-form-item label="楼栋" prop="buildingId"><el-select v-model="form.buildingId"><el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" /></el-select></el-form-item>
        <el-form-item label="楼层" prop="floor"><el-input-number v-model="form.floor" :min="1" /></el-form-item>
        <el-form-item label="床位总数" prop="totalBeds"><el-input-number v-model="form.totalBeds" :min="1" :max="10" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确认</el-button></template>
    </el-dialog>
    <el-dialog title="床位列表" v-model="bedsVisible" width="400px">
      <el-table :data="beds" border stripe>
        <el-table-column prop="bedNo" label="床位号" />
        <el-table-column prop="status" label="状态"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '空闲' : '占用' }}</el-tag></template></el-table-column>
      </el-table>
    </el-dialog>
    <el-dialog :title="studentsTitle" v-model="studentsVisible" width="500px">
      <el-table :data="students" border stripe v-loading="studentsLoading">
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column label="性别"><template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template></el-table-column>
        <el-table-column prop="bedNo" label="床位" width="70" />
      </el-table>
      <div v-if="students.length === 0 && !studentsLoading" style="text-align:center;color:#999;padding:20px">暂无入住学生</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBuildings, getRooms, addRoom, updateRoom, deleteRoom, getRoomBeds, getRoomStudents } from '../../api/dorm'
import { ElMessage, ElMessageBox } from 'element-plus'

const buildings = ref([])
const list = ref({ records: [], total: 0 })
const loading = ref(false)
const query = reactive({ page: 1, size: 50, buildingId: 1 })
const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref(null); const formRef = ref()
const form = reactive({ roomNo: '', buildingId: null, floor: 1, totalBeds: 4 })
const rules = { roomNo: [{ required: true, message: '请输入房间号' }], buildingId: [{ required: true, message: '请选择楼栋' }] }
const bedsVisible = ref(false); const beds = ref([])
const studentsVisible = ref(false); const students = ref([]); const studentsTitle = ref(''); const studentsLoading = ref(false)

const getBuildingName = (id) => { const b = buildings.value.find(i => i.id === id); return b ? b.name : '' }
const fetchBuildings = async () => { buildings.value = await getBuildings() }
const fetchList = async () => { loading.value = true; list.value = await getRooms(query); loading.value = false }

const showDialog = (row) => {
  if (row) { isEdit.value = true; editId.value = row.id; Object.assign(form, { roomNo: row.roomNo, buildingId: row.buildingId, floor: row.floor, totalBeds: row.totalBeds }) }
  else { isEdit.value = false; editId.value = null; Object.assign(form, { roomNo: '', buildingId: null, floor: 1, totalBeds: 4 }) }
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  if (isEdit.value) { await updateRoom(editId.value, form); ElMessage.success('更新成功') } else { await addRoom(form); ElMessage.success('添加成功') }
  dialogVisible.value = false; fetchList()
}
const handleDelete = async (id) => { await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' }); await deleteRoom(id); ElMessage.success('删除成功'); fetchList() }
const showBeds = async (row) => { beds.value = await getRoomBeds(row.id); bedsVisible.value = true }

const showStudents = async (row) => {
  studentsTitle.value = row.roomNo + ' - 住宿学生'
  studentsVisible.value = true
  studentsLoading.value = true
  try { students.value = await getRoomStudents(row.id) } catch { students.value = [] }
  studentsLoading.value = false
}

onMounted(() => { fetchBuildings(); fetchList() })
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>