<template>
  <div>
    <div class="toolbar"><h4>用户管理</h4></div>
    <el-table :data="list.records" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column label="角色"><template #default="{ row }"><el-tag :type="row.role === 0 ? 'danger' : 'success'">{{ row.role === 0 ? '管理员' : '学生' }}</el-tag></template></el-table-column>
      <el-table-column prop="studentNo" label="学号" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="性别"><template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="list.total" layout="prev, pager, next, total" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers } from '../../api/accom'

const list = ref({ records: [], total: 0 }); const loading = ref(false); const page = ref(1); const size = ref(10)
const fetchList = async () => { loading.value = true; list.value = await getUsers({ page: page.value, size: size.value }); loading.value = false }
onMounted(fetchList)
</script>
<style scoped>.toolbar { margin-bottom: 16px; }</style>
