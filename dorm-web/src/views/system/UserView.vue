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
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column label="角色" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.role === 0" type="danger" size="small">总管理员</el-tag>
              <el-tag v-else-if="row.role === 2" type="warning" size="small">二级管理员</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.lockUntil && new Date(row.lockUntil) > new Date()" type="danger" size="small">锁定</el-tag>
              <el-tag v-else type="success" size="small">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button v-if="currentRole === 0 || row.role !== 0" size="small" @click="showEdit(row)">编辑</el-button>
              <el-button v-if="currentRole === 0 || row.role !== 0" size="small" type="warning" @click="showResetPwd(row)">重置密码</el-button>
              <el-popconfirm v-if="currentRole === 0" title="确定删除该用户吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="学生" name="student">
        <el-table :data="list.records" border stripe v-loading="loading">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column label="性别" width="60" align="center">
            <template #default="{ row }">
              <el-icon v-if="row.gender === 1" color="#409eff"><Male /></el-icon>
              <el-icon v-else color="#f56c6c"><Female /></el-icon>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.lockUntil && new Date(row.lockUntil) > new Date()" type="danger" size="small">锁定</el-tag>
              <el-tag v-else type="success" size="small">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showEdit(row)">编辑</el-button>
              <el-button size="small" type="warning" @click="showResetPwd(row)">重置密码</el-button>
              <el-popconfirm v-if="currentRole === 0" title="确定删除该学生吗？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-pagination v-model:current-page="page" :page-size="size" :total="list.total"
      layout="total, prev, pager, next" @current-change="fetchList"
      style="margin-top:16px;justify-content:flex-end" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @closed="clearForm">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="form.username" placeholder="请输入用户名" maxlength="30" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码(6-20位)" show-password maxlength="20" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%" :disabled="isEdit && currentRole === 2">
            <el-option v-if="currentRole === 0" :value="0" label="总管理员" />
            <el-option v-if="currentRole === 0" :value="2" label="二级管理员" />
            <el-option :value="1" label="学生" />
          </el-select>
        </el-form-item>
        <el-form-item label="学号" prop="studentNo" v-if="form.role === 1">
          <el-input v-model="form.studentNo" placeholder="请输入学号" maxlength="30" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" maxlength="20" />
        </el-form-item>
        <el-form-item label="性别" v-if="form.role === 1">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
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
        <el-button type="primary" :loading="resetting" @click="handleResetPwd">确定</el-button>
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
const size = ref(10)
const keyword = ref('')
const activeTab = ref('admin')
const currentRole = ref(parseInt(localStorage.getItem('role') || '0'))

const fetchList = async () => {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (keyword.value) params.keyword = keyword.value
  if (activeTab.value === 'admin') {
    params.role = null
    // 查询管理员（前端过滤 role 0 和 2）
  } else {
    params.role = 1
  }
  try {
    // 管理员tab查询所有非学生，后端不支持!=1，改为无role过滤时前端过滤
    if (activeTab.value === 'admin') {
      const res = await getUsers({ page: page.value, size: 100, keyword: keyword.value })
      list.value = { records: (res.records || []).filter(u => u.role === 0 || u.role === 2), total: 0 }
      list.value.total = list.value.records.length
    } else {
      list.value = await getUsers(params)
    }
  } catch (_) {}
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
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }]
}

const clearForm = () => {
  Object.assign(form, { username: '', password: '', realName: '', role: 1, studentNo: '', phone: '', gender: 0 })
  isEdit.value = false; editId.value = null
  formRef.value?.resetFields()
}

const showCreate = () => {
  dialogTitle.value = '新增用户'
  clearForm()
  dialogVisible.value = true
}

const showEdit = (row) => {
  dialogTitle.value = '编辑用户'
  isEdit.value = true; editId.value = row.id
  Object.assign(form, {
    username: row.username, password: '',
    realName: row.realName, role: row.role,
    studentNo: row.studentNo || '', phone: row.phone || '', gender: row.gender
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(editId.value, {
        realName: form.realName, role: form.role,
        studentNo: form.studentNo || null, phone: form.phone || null, gender: form.gender
      })
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
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