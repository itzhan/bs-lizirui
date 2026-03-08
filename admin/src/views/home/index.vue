<template>
  <div class="home-container">
    <div class="home-card-container">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in cards" :key="item.title">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-info">
                <span class="stat-title">{{ item.title }}</span>
                <span class="stat-value">{{ item.value }}</span>
              </div>
              <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>最近订单</span></template>
          <el-table :data="recentOrders" stripe style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="receiver" label="收货人" width="100" />
            <el-table-column prop="totalAmount" label="金额" width="100">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>系统信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">母婴乐园商城</el-descriptions-item>
            <el-descriptions-item label="后端框架">Spring Boot 3.2</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3 + Element Plus</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
            <el-descriptions-item label="当前版本">v1.0.0</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="home">
import { ref, onMounted } from "vue";
import { ShoppingCart, User, Goods, TrendCharts } from "@element-plus/icons-vue";
import http from "@/api";

const cards = ref([
  { title: "总订单数", value: "0", icon: ShoppingCart, color: "#409eff" },
  { title: "待发货", value: "0", icon: TrendCharts, color: "#e6a23c" },
  { title: "总销售额", value: "¥0", icon: Goods, color: "#67c23a" },
  { title: "用户总数", value: "0", icon: User, color: "#f56c6c" }
]);
const recentOrders = ref([]);

const statusText = (s: number) => ["待付款", "待发货", "已发货", "已完成", "已取消", "已退款"][s] || "未知";
const statusType = (s: number) => (["warning", "info", "", "success", "danger", "danger"] as const)[s] || "info";

onMounted(async () => {
  try {
    const { data: stats } = await http.get<any>("/api/admin/statistics", {}, { loading: false });
    const { data: userStats } = await http.get<any>("/api/admin/statistics/users", {}, { loading: false });
    cards.value[0].value = String(stats.totalOrders || 0);
    cards.value[1].value = String(stats.pendingShipment || 0);
    cards.value[2].value = "¥" + (stats.totalRevenue || 0);
    cards.value[3].value = String(userStats.totalUsers || 0);
  } catch (e) { /* ignore */ }
  try {
    const { data } = await http.get<any>("/api/admin/orders", { page: 1, size: 5 }, { loading: false });
    recentOrders.value = data.records || [];
  } catch (e) { /* ignore */ }
});
</script>

<style scoped>
.home-container { padding: 10px; }
.stat-card { height: 120px; }
.stat-content { display: flex; justify-content: space-between; align-items: center; height: 100%; }
.stat-info { display: flex; flex-direction: column; }
.stat-title { font-size: 14px; color: #909399; margin-bottom: 10px; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
</style>
