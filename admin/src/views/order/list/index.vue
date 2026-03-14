<template>
  <div class="table-box">
    <div class="card table-search" style="margin-bottom: 16px">
      <el-form :inline="true">
        <el-form-item label="订单号"><el-input v-model="searchParams.orderNo" placeholder="订单号" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" placeholder="全部" clearable>
            <el-option v-for="(text, idx) in statusList" :key="idx" :label="text" :value="idx" />
          </el-select>
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="searchParams.keyword" placeholder="收货人/手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="receiver" label="收货人" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusList[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link type="success" v-if="row.status === 1" @click="shipOrder(row.id)">发货</el-button>
            <el-popconfirm
              title="确认退款?"
              @confirm="refundOrder(row.id)"
              v-if="row.status === 5 || row.status === 1 || row.status === 2"
            >
              <template #reference><el-button link type="danger">退款</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="searchParams.page"
        v-model:page-size="searchParams.size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @change="getTableData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentOrder.status)">{{ statusList[currentOrder.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.receiver }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentOrder.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentOrder.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || "无" }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="orderItems" stripe style="margin-top: 16px">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="subtotal" label="小计" width="100">
          <template #default="{ row }">¥{{ row.subtotal }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="orderList">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import http from "@/api";

const statusList = ["待付款", "待发货", "已发货", "已完成", "已取消", "已退款"];
const statusType = (s: number) => (["warning", "info", "", "success", "danger", "danger"] as const)[s] || "info";
const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);
const searchParams = reactive({ page: 1, size: 10, orderNo: "", keyword: "", status: undefined as number | undefined });
const detailVisible = ref(false);
const currentOrder = ref<any>(null);
const orderItems = ref<any[]>([]);

const getTableData = async () => {
  loading.value = true;
  try {
    const { data } = await http.get<any>("/api/admin/orders", searchParams, { loading: false });
    tableData.value = data.records || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
};

const search = () => {
  searchParams.page = 1;
  getTableData();
};
const resetSearch = () => {
  searchParams.orderNo = "";
  searchParams.keyword = "";
  searchParams.status = undefined;
  search();
};

const viewDetail = async (row: any) => {
  const { data } = await http.get<any>("/api/admin/orders/" + row.id, {}, { loading: false });
  currentOrder.value = data.order;
  orderItems.value = data.items || [];
  detailVisible.value = true;
};

const shipOrder = async (id: number) => {
  await http.put("/api/admin/orders/" + id + "/ship");
  ElMessage.success("发货成功");
  getTableData();
};

const refundOrder = async (id: number) => {
  await http.put("/api/admin/orders/" + id + "/refund");
  ElMessage.success("退款成功");
  getTableData();
};

onMounted(getTableData);
</script>
