<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h2>👋 欢迎使用学生宿舍管理系统</h2>
      <p>Dormitory Management System — 高效、便捷的宿舍管理平台</p>
    </div>
    <el-row :gutter="20" class="card-row">
      <el-col :span="8" v-if="role === 0 || role === 2">
        <div class="dash-card card-building" @click="$router.push('/dorm/buildings')"><div class="card-icon">🏢</div><div class="card-info"><div class="card-title">宿舍管理</div><div class="card-desc">维护楼栋、寝室信息，查询空闲床位</div></div><el-icon class="card-arrow"><ArrowRight /></el-icon></div>
      </el-col>
      <el-col :span="8" v-if="role === 0 || role === 2">
        <div class="dash-card card-accom" @click="$router.push('/accom/checkin')"><div class="card-icon">🛏️</div><div class="card-info"><div class="card-title">住宿业务</div><div class="card-desc">办理学生入住、调寝、退宿</div></div><el-icon class="card-arrow"><ArrowRight /></el-icon></div>
      </el-col>
      <el-col :span="8">
        <div class="dash-card card-repair" @click="$router.push(role === 0 ? '/repair/manage' : '/repair/my')"><div class="card-icon">🔧</div><div class="card-info"><div class="card-title">报修服务</div><div class="card-desc">{{ role === 0 ? '处置维修工单，跟踪维修进度' : '提交报修申请，查看处理状态' }}</div></div><el-icon class="card-arrow"><ArrowRight /></el-icon></div>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="card-row" v-if="role === 0 || role === 2">
      <el-col :span="8">
        <div class="dash-card card-user" @click="$router.push('/system/users')"><div class="card-icon">👥</div><div class="card-info"><div class="card-title">用户管理</div><div class="card-desc">管理系统用户账号与权限</div></div><el-icon class="card-arrow"><ArrowRight /></el-icon></div>
      </el-col>
      <el-col :span="8">
        <div class="dash-card card-profile" @click="$router.push('/profile')"><div class="card-icon">⚙️</div><div class="card-info"><div class="card-title">个人中心</div><div class="card-desc">查看与修改个人账户信息</div></div><el-icon class="card-arrow"><ArrowRight /></el-icon></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const role = parseInt(localStorage.getItem('role') || '-1')
</script>

<style scoped>
.dashboard { max-width: 1100px; margin: 0 auto; }
.welcome-section { margin-bottom: 32px; padding: 28px 32px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 12px; color: #fff; }
.welcome-section h2 { margin: 0 0 8px; font-size: 22px; font-weight: 700; }
.welcome-section p { margin: 0; font-size: 14px; opacity: 0.85; }
.card-row { margin-bottom: 20px; }
.dash-card { display: flex; align-items: center; gap: 16px; padding: 24px 20px; background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; }
.dash-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.1); }
.card-icon { font-size: 40px; flex-shrink: 0; }
.card-info { flex: 1; min-width: 0; }
.card-title { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.card-desc { font-size: 13px; color: #909399; line-height: 1.5; }
.card-arrow { color: #c0c4cc; font-size: 18px; flex-shrink: 0; transition: color 0.2s; }
.dash-card:hover .card-arrow { color: #409EFF; }
</style>