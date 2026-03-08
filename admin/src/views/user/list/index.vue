<template>
  <div class="table-box">
    <div class="card table-search" style="margin-bottom: 16px;">
      <el-form :inline="true">
        <el-form-item label="关键词"><el-input v-model="searchParams.keyword" placeholder="用户名/昵称/手机号" clearable @keyup.enter="search" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="role" label="角色" width="80">
          <template #default="{ row }"><el-tag :type="row.role === 'ADMIN' ? 'danger' : ''">{{ row.role }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" :disabled="row.role === 'ADMIN'" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" />
      </el-table>
      <el-pagination v-model:current-page="searchParams.page" v-model:page-size="searchParams.size"
        :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next" @change="getTableData"
        style="margin-top: 16px; justify-content: flex-end;" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="userList">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import http from "@/api";

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const searchParams = reactive({ page: 1, size: 10, keyword: "", status: undefined as number | undefined });

const getTableData = async () => {
  loading.value = true;
  try {
    const { data } = await http.get<any>("/api/admin/users", searchParams, { loading: false });
    tableData.value = data.records || [];
    total.value = data.total || 0;
  } finally { loading.value = false; }
};

const search = () => { searchParams.page = 1; getTableData(); };
const resetSearch = () => { searchParams.keyword = ""; searchParams.status = undefined; search(); };

const toggleStatus = async (row: any) => {
  await http.put("/api/admin/users/" + row.id + "/status", { status: row.status === 1 ? 0 : 1 });
  ElMessage.success("操作成功");
  getTableData();
};

onMounted(getTableData);
</script>
