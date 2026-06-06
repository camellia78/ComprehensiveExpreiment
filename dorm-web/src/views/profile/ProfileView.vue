<template>
  <div style="max-width:500px">
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h4>个人中心</h4>
      <el-button type="primary" @click="showPwdDialog">修改密码</el-button>
    </div>
    <el-descriptions :column="1" border style="margin-top:16px">
      <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user.role === 0 ? '管理员' : '学生' }}</el-descriptions-item>
      <el-descriptions-item label="学号">{{ user.studentNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机">{{ user.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ user.gender === 1 ? '男' : '女' }}</el-descriptions-item>
    </el-descriptions>

    <el-dialog title="修改密码" v-model="pwdVisible" width="400px" @closed="resetPwdForm">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="6-20位新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="changing" @click="handleChangePwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProfile, changePassword } from '../../api/auth'
import { ElMessage } from 'element-plus'

const user = ref({})
onMounted(async () => { user.value = await getProfile() })

const pwdVisible = ref(false)
const changing = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次密码输入不一致'))
  else callback()
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const showPwdDialog = () => { pwdVisible.value = true }
const resetPwdForm = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.resetFields()
}

const handleChangePwd = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  changing.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdVisible.value = false
    localStorage.clear()
    window.location.hash = '#/login'
  } catch (e) { /* handled */ }
  finally { changing.value = false }
}
</script>
