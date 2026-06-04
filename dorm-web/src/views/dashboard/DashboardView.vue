<template>
  <div>
    <h3>欢迎使用学生宿舍管理系统</h3>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8" v-if="role === 0">
        <el-card><template #header>宿舍管理</template><p>维护楼栋、寝室信息，查询空闲床位</p><el-button type="primary" @click="$router.push('/dorm/buildings')">进入</el-button></el-card>
      </el-col>
      <el-col :span="8" v-if="role === 0">
        <el-card><template #header>住宿业务</template><p>办理学生入住、调宿、退宿</p><el-button type="primary" @click="$router.push('/accom/checkin')">进入</el-button></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><template #header>报修服务</template><p>{{ role === 0 ? '处置维修工单' : '提交报修申请' }}</p><el-button type="primary" @click="$router.push(role === 0 ? '/repair/manage' : '/repair/my')">进入</el-button></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const role = parseInt(localStorage.getItem('role') || '-1')
</script>
