<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#304156">
      <div class="logo">宿舍管理系统</div>
      <el-menu :default-active="route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router>
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><span>工作台</span></el-menu-item>
        <template v-if="role === 0 || role === 2">
          <el-sub-menu index="dorm"><template #title><el-icon><OfficeBuilding /></el-icon><span>宿舍管理</span></template>
            <el-menu-item index="/dorm/buildings">楼栋管理</el-menu-item>
            <el-menu-item index="/dorm/rooms">寝室管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="accom"><template #title><el-icon><UserFilled /></el-icon><span>住宿业务</span></template>
            <el-menu-item index="/accom/checkin">入住登记</el-menu-item>
            <el-menu-item index="/accom/transfer">调寝处理</el-menu-item>
            <el-menu-item index="/accom/checkout">退宿登记</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/repair/manage"><el-icon><Tools /></el-icon><span>工单处置</span></el-menu-item>
          <el-sub-menu index="system" v-if="role === 0 || role === 2">
            <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
            <el-menu-item index="/system/users">用户管理</el-menu-item>
          </el-sub-menu>
        </template>
        <template v-if="role === 1">
          <el-menu-item index="/repair/my"><el-icon><Tools /></el-icon><span>报修申请</span></el-menu-item>
        </template>
        <el-menu-item index="/profile"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="border-bottom:1px solid #e4e7ed;display:flex;align-items:center;justify-content:flex-end">
        <span style="margin-right:12px">{{ user?.realName }} {{ roleLabel }}</span>
        <el-button text @click="handleLogout">退出</el-button>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { HomeFilled, OfficeBuilding, UserFilled, Tools, User, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const user = computed(() => authStore.user)
const role = computed(() => parseInt(localStorage.getItem('role') || '-1'))
const roleLabel = computed(() => {
  if (role.value === 0) return '(总管理员)'
  if (role.value === 2) return '(管理员)'
  return '(学生)'
})
const handleLogout = () => { authStore.doLogout(); router.push('/login') }
</script>

<style scoped>
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; letter-spacing: 2px; border-bottom: 1px solid rgba(255,255,255,0.1); }
</style>