<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span class="logo-icon" v-if="!isCollapse">&#127968;</span>
        <span class="logo-text" v-if="!isCollapse">宿舍管理系统</span>
        <span class="logo-icon" v-else>&#127968;</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        background-color="transparent"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><span>工作台</span></el-menu-item>
        <template v-if="role === 0 || role === 2">
          <template v-if="role === 0">
            <el-sub-menu index="dorm"><template #title><el-icon><OfficeBuilding /></el-icon><span>宿舍管理</span></template>
              <el-menu-item index="/dorm/buildings">楼栋管理</el-menu-item>
              <el-menu-item index="/dorm/rooms">寝室管理</el-menu-item>
            </el-sub-menu>
          </template>
          <el-sub-menu index="accom"><template #title><el-icon><UserFilled /></el-icon><span>住宿业务</span></template>
            <el-menu-item index="/accom/checkin">入住登记</el-menu-item>
            <el-menu-item index="/accom/transfer">调宿处理</el-menu-item>
            <el-menu-item index="/accom/checkout">退宿登记</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/repair/manage"><el-icon><Tools /></el-icon><span>工单处置</span></el-menu-item>
          <el-menu-item index="/system/users"><el-icon><Avatar /></el-icon><span>用户管理</span></el-menu-item>
        </template>
        <template v-if="role === 1">
          <el-menu-item index="/repair/my"><el-icon><Tools /></el-icon><span>报修申请</span></el-menu-item>
        </template>
        <el-menu-item index="/profile"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="topbar-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
        </div>
        <div class="topbar-right">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ user && user.realName }}
            <el-tag :type="role === 0 ? 'danger' : role === 2 ? 'warning' : 'success'" size="small" style="margin-left:8px">{{ role === 0 ? '超级管理员' : role === 2 ? '二级管理员' : '学生' }}</el-tag>
          </span>
          <el-button text type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-content"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const user = computed(() => authStore.user)
const role = computed(() => parseInt(localStorage.getItem('role') || '-1'))
const isCollapse = ref(false)
const handleLogout = () => { authStore.logout(); router.push('/login') }
</script>

<style scoped>
.main-layout { height: 100vh; }
.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  transition: width 0.3s ease;
  overflow: hidden;
}
.logo { height: 60px; display: flex; align-items: center; justify-content: center; gap: 8px; border-bottom: 1px solid rgba(255,255,255,0.08); color: #fff; font-size: 17px; font-weight: 700; letter-spacing: 1px; }
.logo-icon { font-size: 24px; }
.el-menu { border-right: none; }
.el-menu .el-menu-item { margin: 2px 8px; border-radius: 8px; }
.el-menu .el-menu-item:hover { background: rgba(255,255,255,0.08) !important; }
.el-menu .el-menu-item.is-active { background: rgba(64,158,255,0.2) !important; }
.el-menu .el-sub-menu .el-menu-item { min-width: auto; margin: 2px 8px; border-radius: 8px; }
.topbar { display: flex; align-items: center; justify-content: space-between; height: 56px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); padding: 0 20px; }
.collapse-btn { cursor: pointer; color: #606266; transition: color 0.2s; }
.collapse-btn:hover { color: #409EFF; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.user-info { display: flex; align-items: center; gap: 6px; color: #606266; font-size: 14px; }
.main-content { background: #f5f7fa; padding: 20px; min-height: calc(100vh - 56px); }
</style>