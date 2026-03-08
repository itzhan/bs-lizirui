<template>
  <div class="table-box">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>公告管理</span>
          <el-button type="success" @click="openDialog('add')">新增公告</el-button>
        </div>
      </template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }"><el-tag>{{ ["", "通知", "活动", "系统"][row.type] || "其他" }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="标题"><el-input v-model="formData.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="formData.type"><el-option label="通知" :value="1" /><el-option label="活动" :value="2" /><el-option label="系统" :value="3" /></el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="formData.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="草稿" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="announcement">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import http from "@/api";

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const searchParams = reactive({ page: 1, size: 10 });
const dialogVisible = ref(false);
const dialogTitle = ref("新增公告");
const formData = reactive<any>({ title: "", content: "", type: 1, status: 1 });

const getTableData = async () => {
  loading.value = true;
  try {
    const { data } = await http.get<any>("/api/admin/announcements", searchParams, { loading: false });
    tableData.value = data.records || [];
    total.value = data.total || 0;
  } finally { loading.value = false; }
};

const openDialog = (type: string, row?: any) => {
  dialogTitle.value = type === "add" ? "新增公告" : "编辑公告";
  if (row) Object.assign(formData, row);
  else Object.assign(formData, { id: undefined, title: "", content: "", type: 1, status: 1 });
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (formData.id) await http.put("/api/admin/announcements/" + formData.id, formData);
  else await http.post("/api/admin/announcements", formData);
  ElMessage.success("操作成功");
  dialogVisible.value = false;
  getTableData();
};

const deleteItem = async (id: number) => {
  await http.delete("/api/admin/announcements/" + id);
  ElMessage.success("删除成功");
  getTableData();
};

onMounted(getTableData);
</script>
