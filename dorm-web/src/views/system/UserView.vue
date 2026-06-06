<template>
  <div>
    <div class="toolbar">
      <h4>鐢ㄦ埛绠＄悊</h4>
      <div style="display:flex;gap:12px">
        <el-input v-model="keyword" placeholder="鎼滅储鐢ㄦ埛鍚?濮撳悕/瀛﹀彿/鐢佃瘽" clearable style="width:260px" @clear="fetchList" @keyup.enter="fetchList">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="roleFilter" placeholder="瑙掕壊绛涢€? clearable style="width:120px" @change="fetchList">
          <el-option :value="0" label="绠＄悊鍛? />
          <el-option :value="1" label="瀛︾敓" />
        </el-select>
        <el-button type="primary" @click="showCreate">鏂板鐢ㄦ埛</el-button>
      </div>
    </div>

    <el-table :data="list.records" border stripe v-loading="loading">
      <el-table-column prop="id" label="鐢ㄦ埛ID" width="100">
        <template #default="{ row }">
          <el-text truncated style="max-width:80px">{{ String(row.id).slice(-8) }}</el-text>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="鐢ㄦ埛鍚? width="110" />
      <el-table-column prop="realName" label="濮撳悕" width="100" />
      <el-table-column label="瑙掕壊" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 0 ? 'danger' : ''" size="small">{{ row.role === 0 ? '绠＄悊鍛? : '瀛︾敓' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="studentNo" label="瀛﹀彿" width="120">
        <template #default="{ row }">{{ row.studentNo || '-' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="鐢佃瘽" width="130" />
      <el-table-column label="鎬у埆" width="60" align="center">
        <template #default="{ row }">
          <el-icon v-if="row.gender === 1" color="#409eff"><Male /></el-icon>
          <el-icon v-else color="#f56c6c"><Female /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="鐘舵€? width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.lockUntil && new Date(row.lockUntil) > new Date()" type="danger" size="small">閿佸畾</el-tag>
          <el-tag v-else type="success" size="small">姝ｅ父</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="鍒涘缓鏃堕棿" prop="createTime" width="170" />
      <el-table-column label="鎿嶄綔" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showEdit(row)">缂栬緫</el-button>
          <el-button size="small" type="warning" @click="showResetPwd(row)">閲嶇疆瀵嗙爜</el-button>
          <el-popconfirm title="纭畾鍒犻櫎璇ョ敤鎴峰悧锛? @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">鍒犻櫎</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" :total="list.total"
      layout="total, prev, pager, next" @current-change="fetchList"
      style="margin-top:16px;justify-content:flex-end" />

    <!-- 鏂板/缂栬緫寮圭獥 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="480px" @closed="resetForm">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="鐢ㄦ埛鍚? prop="username" v-if="!isEdit">
          <el-input v-model="form.username" placeholder="璇疯緭鍏ョ敤鎴峰悕" maxlength="30" />
        </el-form-item>
        <el-form-item label="瀵嗙爜" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="璇疯緭鍏ュ瘑鐮?6-20浣?" show-password maxlength="20" />
        </el-form-item>
        <el-form-item label="濮撳悕" prop="realName">
          <el-input v-model="form.realName" placeholder="璇疯緭鍏ュ鍚? maxlength="20" />
        </el-form-item>
        <el-form-item label="瑙掕壊" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option :value="0" label="绠＄悊鍛? />
            <el-option :value="1" label="瀛︾敓" />
          </el-select>
        </el-form-item>
        <el-form-item label="瀛﹀彿" prop="studentNo" v-if="form.role === 1">
          <el-input v-model="form.studentNo" placeholder="璇疯緭鍏ュ鍙? maxlength="30" />
        </el-form-item>
        <el-form-item label="鐢佃瘽">
          <el-input v-model="form.phone" placeholder="璇疯緭鍏ョ數璇? maxlength="20" />
        </el-form-item>
        <el-form-item label="鎬у埆">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">鐢?/el-radio>
            <el-radio :value="0">濂?/el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '淇濆瓨' : '鍒涘缓' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 閲嶇疆瀵嗙爜寮圭獥 -->
    <el-dialog title="閲嶇疆瀵嗙爜" v-model="resetVisible" width="400px" @closed="resetForm.value?.resetFields()">
      <el-form :model="pwdForm" :rules="resetRules" ref="resetFormRef" label-width="80px">
        <el-form-item label="鏂板瘑鐮? prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="璇疯緭鍏ユ柊瀵嗙爜(6-20浣?" show-password maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">鍙栨秷</el-button>
        <el-button type="primary" :loading="resetting" @click="handleResetPwd">纭畾</el-button>
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
const roleFilter = ref(null)

const fetchList = async () => {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (keyword.value) params.keyword = keyword.value
  if (roleFilter.value !== null && roleFilter.value !== '') params.role = roleFilter.value
  try { list.value = await getUsers(params) } catch (_) {}
  loading.value = false
}

onMounted(fetchList)

// ---------- 鏂板 / 缂栬緫 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('鏂板鐢ㄦ埛')
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '', realName: '', role: 1, studentNo: '', phone: '', gender: 0 })
const formRules = {
  username: [{ required: true, message: '璇疯緭鍏ョ敤鎴峰悕', trigger: 'blur' }],
  password: [{ required: true, message: '璇疯緭鍏ュ瘑鐮?, trigger: 'blur' }],
  realName: [{ required: true, message: '璇疯緭鍏ュ鍚?, trigger: 'blur' }],
  role: [{ required: true, message: '璇烽€夋嫨瑙掕壊', trigger: 'change' }],
  studentNo: [{ required: true, message: '璇疯緭鍏ュ鍙?, trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, { username: '', password: '', realName: '', role: 1, studentNo: '', phone: '', gender: 0 })
  isEdit.value = false; editId.value = null
  formRef.value?.resetFields()
}

const showCreate = () => {
  dialogTitle.value = '鏂板鐢ㄦ埛'
  resetForm()
  dialogVisible.value = true
}

const showEdit = (row) => {
  dialogTitle.value = '缂栬緫鐢ㄦ埛'
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
      ElMessage.success('鏇存柊鎴愬姛')
    } else {
      await createUser(form)
      ElMessage.success('鍒涘缓鎴愬姛')
    }
    dialogVisible.value = false
    fetchList()
  } catch (_) {} finally { submitting.value = false }
}

// ---------- 鍒犻櫎 ----------
const handleDelete = async (id) => {
  try { await deleteUser(id); ElMessage.success('鍒犻櫎鎴愬姛'); fetchList() } catch (_) {}
}

// ---------- 閲嶇疆瀵嗙爜 ----------
const resetVisible = ref(false)
const resetUserId = ref(null)
const resetting = ref(false)
const resetFormRef = ref()
const pwdForm = reactive({ newPassword: '' })
const resetRules = { newPassword: [{ required: true, message: '璇疯緭鍏ユ柊瀵嗙爜', trigger: 'blur' }] }

const showResetPwd = (row) => { resetUserId.value = row.id; pwdForm.newPassword = ''; resetVisible.value = true }
const handleResetPwd = async () => {
  const valid = await resetFormRef.value.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try { await resetPassword(resetUserId.value, { newPassword: pwdForm.newPassword }); ElMessage.success('瀵嗙爜閲嶇疆鎴愬姛'); resetVisible.value = false } catch (_) {}
  finally { resetting.value = false }
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar h4 { margin: 0; }
</style>