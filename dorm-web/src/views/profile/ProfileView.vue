<template>
  <div style="max-width:500px">
    <h4>个人中心</h4>
    <el-descriptions :column="1" border style="margin-top:16px">
      <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user.role === 0 ? '管理员' : '学生' }}</el-descriptions-item>
      <el-descriptions-item label="学号">{{ user.studentNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机">{{ user.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ user.gender === 1 ? '男' : '女' }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProfile } from '../../api/auth'

const user = ref({})
onMounted(async () => { user.value = await getProfile() })
</script>
