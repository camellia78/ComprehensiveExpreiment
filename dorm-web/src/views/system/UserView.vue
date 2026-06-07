<template>
  <div>
    <div class="toolbar">
      <h4>用户管理</h4>
      <div style="display:flex;gap:12px">
        <el-input v-model="keyword" placeholder="搜索用户名/姓名/学号/电话" clearable style="width:260px" @clear="fetchList" @keyup.enter="fetchList">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="showCreate">新增用户</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="管理员" name="admin">
        <el-table :data="list.records" border stripe v-loading="loading">
          <el-table-column prop="username" label="用户名" min-width="100" />
          <el-table-column prop="realName" label="姓名" min-width="80" />
          <el-table-column label="角色" min-width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.role === 0" type="danger" size="small">总管理员</el-tag>
              <el-tag v-else-if="row.role === 2" type="warning" size="small">二级管理员</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="电话" min-width="140" />
          <el-table-column label="创建时间" prop="createTime" min-width="170" />
          <el-table-column label="操作" min-width="240">
            <template #default="{ row }">
              <el-button v-if="currentRole === 0 || row.role !== 0" size="small" @click="showEdit(row)">编辑</el-button>
              <el-button v-if="currentRole === 0 || row.role !== 0" size="small" type="warning" @click="showResetPwd(row)">重置密码</el-button>
              <el-popconfirm v-if="currentRole === 0 && row.role !== 0" title="确定删除该用户吗？" @confirm="handleDelete(row.id)">
                <template #reference><el-button size="small" type="danger">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="学生" name="student">
        <el-table :data="list.records" border stripe v-loading="loading">
          <el-table-column prop="username" label="用户名" min-width="100" />
          <el-table-column prop="realName" label="姓名" min-width="80" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="phone" label="电话" min-width="140" />
          <el-table-column label="性别" width="60" align="center">
            <template #default="{ row }">
              <el-icon v-if="row.gender === 1" color="#409eff"><Male /></el-icon>
              <el-icon v-else color="#f56c6c"><Female /></el-icon>
            </template>
          </el-table-column>
          <el-table-column label="所在宿舍" width="160">
            <template #default="{ row }">
              <span v-if="row.buildingName">{{ row.buildingName }} {{ row.roomNo }}室 {{ row.bedNo }}床</span>
              <el-tag v-else type="info" size="small">未入住</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" min-width="170" />
          <el-table-column label="操作" min-width="240">
            <template #default="{ row }">
              <el-button size="small" @click="showEdit(row)">编辑</el-button>
              <el-button size="small" type="warning" @click="showResetPwd(row)">重置密码</el-button>
              <el-popconfirm v-if="currentRole === 0" title="确定删除该学生吗？" @confirm="handleDelete(row.id)">
                <template #reference><el-button size="small" type="danger">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="list.total"
      layout="total, prev, pager, next"
      @current-change="fetchList"
      style="margin-top:16px;justify-content:flex-end"
    />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @closed="clearForm">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" maxlength="30" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码(6-20位)" show-password maxlength="20" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" :disabled="isEdit && currentRole !== 0">
            <el-option v-if="currentRole === 0 && !hasSuperAdmin" label="总管理员" :value="0" />
            <el-option v-if="currentRole === 0" label="二级管理员" :value="2" />
            <el-option label="学生" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号" prop="studentNo" v-if="form.role === 1">
          <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="20" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">男</el-radio>
            <el-radio :value="1">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog title="重置密码" v-model="resetVisible" width="400px">
      <el-form :model="pwdForm" :rules="resetRules" ref="resetFormRef" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码(6-20位)" show-password maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="handleResetPwd">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsers, createUser, updateUser, deleteUser, resetPassword } from '../../api/auth'
import { ElMessage } from 'element-plus'
import { Search, Male, Female } from '@element-plus/icons-vue'

const list = ref({ records: [], total: 0 })
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const activeTab = ref('admin')
const currentRole = ref(parseInt(localStorage.getItem('role') || '0'))
const hasSuperAdmin = ref(false)

const fetchList = async () => {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (keyword.value) params.keyword = keyword.value
  try {
    if (activeTab.value === 'student') {
      params.role = 1
      const res = await getUsers(params)
      list.value = { records: res.records || [], total: Number(res.total) || 0 }
    } else {
      params.size = 50
      const res = await getUsers(params)
      const admins = (res.records || []).filter(u => u.role === 0 || u.role === 2)
      const start = (page.value - 1) * size.value
      list.value = { records: admins.slice(start, start + size.value), total: admins.length }
      hasSuperAdmin.value = admins.some(u => u.role === 0)
    }
  } catch (_) { list.value = { records: [], total: 0 } }
  loading.value = false
}

const onTabChange = () => { page.value = 1; fetchList() }
onMounted(fetchList)

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '', realName: '', role: 1, studentNo: '', phone: '', gender: 0 })
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const clearForm = () => {
  Object.assign(form, { username: '', password: '', realName: '', role: 1, studentNo: '', phone: '', gender: 0 })
  isEdit.value = false; editId.value = null
  formRef.value?.resetFields()
}

const showCreate = () => { dialogTitle.value = '新增用户'; clearForm(); dialogVisible.value = true }

const showEdit = (row) => {
  dialogTitle.value = '编辑用户'; isEdit.value = true; editId.value = row.id
  Object.assign(form, { username: row.username, password: '', realName: row.realName, role: row.role,
    studentNo: row.studentNo || '', phone: row.phone || '', gender: row.gender })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(editId.value, { realName: form.realName, role: form.role,
        studentNo: form.studentNo || null, phone: form.phone || null, gender: form.gender })
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false; fetchList()
  } catch (_) {} finally { submitting.value = false }
}

const handleDelete = async (id) => {
  try { await deleteUser(id); ElMessage.success('删除成功'); fetchList() } catch (_) {}
}

const resetVisible = ref(false)
const resetUserId = ref(null)
const resetting = ref(false)
const resetFormRef = ref()
const pwdForm = reactive({ newPassword: '' })
const resetRules = { newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }] }

const showResetPwd = (row) => { resetUserId.value = row.id; pwdForm.newPassword = ''; resetVisible.value = true }
const handleResetPwd = async () => {
  const valid = await resetFormRef.value.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try { await resetPassword(resetUserId.value, { newPassword: pwdForm.newPassword }); ElMessage.success('密码重置成功'); resetVisible.value = false } catch (_) {}
  finally { resetting.value = false }
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar h4 { margin: 0; }
</style>