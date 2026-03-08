<template>
  <div class="table-box">
    <div class="card table-search" style="margin-bottom: 16px;">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="searchParams.keyword" placeholder="商品名称/品牌" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchParams.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" placeholder="全部" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="openDialog('add')">新增商品</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog('edit', row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="deleteProduct(row.id)">
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
        <el-form-item label="名称"><el-input v-model="formData.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formData.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌"><el-input v-model="formData.brand" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="formData.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="formData.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="formData.stock" :min="0" /></el-form-item>
        <el-form-item label="推荐"><el-switch v-model="formData.recommend" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="formData.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="productList">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import http from "@/api";

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const categories = ref<any[]>([]);
const searchParams = reactive({ page: 1, size: 10, keyword: "", categoryId: undefined as number | undefined, status: undefined as number | undefined });
const dialogVisible = ref(false);
const dialogTitle = ref("新增商品");
const formData = reactive<any>({ name: "", categoryId: null, brand: "", price: 0, originalPrice: 0, stock: 0, recommend: 0, description: "" });

const getTableData = async () => {
  loading.value = true;
  try {
    const { data } = await http.get<any>("/api/admin/products", searchParams, { loading: false });
    tableData.value = data.records || [];
    total.value = data.total || 0;
  } finally { loading.value = false; }
};

const getCategories = async () => {
  try {
    const { data } = await http.get<any>("/api/admin/categories", {}, { loading: false });
    categories.value = data || [];
  } catch (e) { /* ignore */ }
};

const search = () => { searchParams.page = 1; getTableData(); };
const resetSearch = () => { searchParams.keyword = ""; searchParams.categoryId = undefined; searchParams.status = undefined; search(); };

const openDialog = (type: string, row?: any) => {
  dialogTitle.value = type === "add" ? "新增商品" : "编辑商品";
  if (row) { Object.assign(formData, row); } else { Object.assign(formData, { id: undefined, name: "", categoryId: null, brand: "", price: 0, originalPrice: 0, stock: 0, recommend: 0, description: "" }); }
  dialogVisible.value = true;
};

const submitForm = async () => {
  try {
    if (formData.id) { await http.put("/api/admin/products/" + formData.id, formData); }
    else { await http.post("/api/admin/products", formData); }
    ElMessage.success("操作成功");
    dialogVisible.value = false;
    getTableData();
  } catch (e) { /* handled by interceptor */ }
};

const toggleStatus = async (row: any) => {
  await http.put("/api/admin/products/" + row.id + "/status", { status: row.status === 1 ? 0 : 1 });
  getTableData();
};

const deleteProduct = async (id: number) => {
  await http.delete("/api/admin/products/" + id);
  ElMessage.success("删除成功");
  getTableData();
};

onMounted(() => { getCategories(); getTableData(); });
</script>
