<template>
  <div class="table-box">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>分类管理</span>
          <el-button type="success" @click="openDialog('add')">新增分类</el-button>
        </div>
      </template>
      <el-table :data="tableData" stripe v-loading="loading" row-key="id">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="分类名称" width="200" />
        <el-table-column prop="parentId" label="父分类ID" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? "启用" : "禁用" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="deleteItem(row.id)">
              <template #reference><el-button link type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="名称"><el-input v-model="formData.name" /></el-form-item>
        <el-form-item label="父分类">
          <el-select v-model="formData.parentId" placeholder="顶级分类" clearable>
            <el-option label="顶级分类" :value="0" />
            <el-option v-for="c in tableData" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="formData.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="productCategory">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import http from "@/api";

const loading = ref(false);
const tableData = ref<any[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref("新增分类");
const formData = reactive<any>({ name: "", parentId: 0, sort: 0, status: 1 });

const getData = async () => {
  loading.value = true;
  try {
    const { data } = await http.get<any>("/api/admin/categories", {}, { loading: false });
    tableData.value = data || [];
  } finally {
    loading.value = false;
  }
};

const openDialog = (type: string, row?: any) => {
  dialogTitle.value = type === "add" ? "新增分类" : "编辑分类";
  if (row) Object.assign(formData, row);
  else Object.assign(formData, { id: undefined, name: "", parentId: 0, sort: 0, status: 1 });
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (formData.id) await http.put("/api/admin/categories/" + formData.id, formData);
  else await http.post("/api/admin/categories", formData);
  ElMessage.success("操作成功");
  dialogVisible.value = false;
  getData();
};

const deleteItem = async (id: number) => {
  await http.delete("/api/admin/categories/" + id);
  ElMessage.success("删除成功");
  getData();
};

onMounted(getData);
</script>
