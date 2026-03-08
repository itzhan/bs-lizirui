<template>
  <div class="table-box">
    <el-card shadow="never">
      <template #header><span>评论管理</span></template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="productId" label="商品ID" width="80" />
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }"><el-rate v-model="row.rating" disabled /></template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评论时间" width="180" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认删除?" @confirm="deleteItem(row.id)">
              <template #reference><el-button link type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="searchParams.page" v-model:page-size="searchParams.size"
        :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next" @change="getTableData"
        style="margin-top: 16px; justify-content: flex-end;" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="reviewList">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import http from "@/api";

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const searchParams = reactive({ page: 1, size: 10 });

const getTableData = async () => {
  loading.value = true;
  try {
    const { data } = await http.get<any>("/api/admin/reviews", searchParams, { loading: false });
    tableData.value = data.records || [];
    total.value = data.total || 0;
  } finally { loading.value = false; }
};

const toggleStatus = async (row: any) => {
  await http.put("/api/admin/reviews/" + row.id + "/status", { status: row.status === 1 ? 0 : 1 });
  ElMessage.success("操作成功");
  getTableData();
};

const deleteItem = async (id: number) => {
  await http.delete("/api/admin/reviews/" + id);
  ElMessage.success("删除成功");
  getTableData();
};

onMounted(getTableData);
</script>
