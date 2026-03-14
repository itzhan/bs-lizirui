<template>
  <div class="home-container">
    <!-- Welcome Banner -->
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <div>
          <h2 class="welcome-title">{{ greeting }}，{{ userName }}！👋</h2>
          <p class="welcome-desc">欢迎使用母婴乐园后台管理系统，今天也要加油哦~</p>
        </div>
        <div class="welcome-date">
          <el-icon :size="18"><Calendar /></el-icon>
          <span>{{ currentDate }}</span>
        </div>
      </div>
    </el-card>

    <!-- Statistics Cards -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6" v-for="item in cards" :key="item.title">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-info">
              <span class="stat-title">{{ item.title }}</span>
              <span class="stat-value" :style="{ color: item.color }">{{ item.value }}</span>
              <span v-if="item.extra" class="stat-extra">{{ item.extra }}</span>
            </div>
            <div class="stat-icon-wrap" :style="{ background: item.color + '15' }">
              <el-icon :size="28" :color="item.color"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Main Content Row -->
    <el-row :gutter="16" style="margin-top: 16px">
      <!-- Recent Orders -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="18" color="#409EFF"><List /></el-icon>
                <span>最近订单</span>
              </div>
              <el-tag size="small" type="info">最近 10 条</el-tag>
            </div>
          </template>
          <el-table :data="recentOrders" stripe style="width: 100%" size="small">
            <el-table-column prop="orderNo" label="订单号" width="200">
              <template #default="{ row }">
                <span style="font-family: monospace; font-size: 12px; color: #606266">{{ row.orderNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="receiver" label="收货人" width="90" />
            <el-table-column prop="totalAmount" label="金额" width="100">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: 600">¥{{ row.totalAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间">
              <template #default="{ row }">
                <span style="font-size: 12px; color: #909399">{{ row.createdAt }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- Right Column -->
      <el-col :span="8">
        <!-- Quick Actions -->
        <el-card shadow="hover" style="margin-bottom: 16px">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="18" color="#e6a23c"><Operation /></el-icon>
                <span>快捷操作</span>
              </div>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/product/list')">
              <el-icon :size="24" color="#409EFF"><Goods /></el-icon>
              <span>商品管理</span>
            </div>
            <div class="action-item" @click="$router.push('/order/list')">
              <el-icon :size="24" color="#e6a23c"><ShoppingCart /></el-icon>
              <span>订单管理</span>
            </div>
            <div class="action-item" @click="$router.push('/user/list')">
              <el-icon :size="24" color="#67c23a"><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="action-item" @click="$router.push('/content/banner')">
              <el-icon :size="24" color="#f56c6c"><Picture /></el-icon>
              <span>轮播图</span>
            </div>
          </div>
        </el-card>

        <!-- System Info -->
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="18" color="#909399"><InfoFilled /></el-icon>
                <span>系统信息</span>
              </div>
            </div>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="系统名称">
              <el-tag size="small" type="danger">母婴乐园商城</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="后端框架">Spring Boot 3.2</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3 + Element Plus</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
            <el-descriptions-item label="管理端模板">Geeker Admin</el-descriptions-item>
            <el-descriptions-item label="当前版本">
              <el-tag size="small">v1.0.0</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- Bottom Row: Hot Products + Order Status Distribution -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="18" color="#f56c6c"><TrendCharts /></el-icon>
                <span>热销商品 TOP5</span>
              </div>
            </div>
          </template>
          <div v-for="(p, idx) in hotProducts" :key="p.id" class="hot-product-item">
            <span class="hot-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
            <div class="hot-info">
              <span class="hot-name">{{ p.name }}</span>
              <span class="hot-brand">{{ p.brand }}</span>
            </div>
            <div class="hot-sales">
              <span class="hot-sales-num">{{ p.sales }}</span>
              <span class="hot-sales-label">件</span>
            </div>
          </div>
          <el-empty v-if="!hotProducts.length" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="18" color="#67c23a"><PieChart /></el-icon>
                <span>订单状态分布</span>
              </div>
            </div>
          </template>
          <div class="order-dist">
            <div v-for="s in orderDist" :key="s.label" class="dist-item">
              <div class="dist-bar">
                <div class="dist-fill" :style="{ width: s.pct + '%', background: s.color }" />
              </div>
              <div class="dist-meta">
                <span class="dist-label">
                  <span class="dist-dot" :style="{ background: s.color }" />
                  {{ s.label }}
                </span>
                <span class="dist-count">{{ s.count }} 单 ({{ s.pct }}%)</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!orderDist.length" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="home">
import { ref, computed, onMounted } from "vue";
import {
  ShoppingCart,
  User,
  Goods,
  TrendCharts,
  List,
  Calendar,
  Operation,
  Picture,
  InfoFilled,
  PieChart
} from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";
import http from "@/api";

const userStore = useUserStore();
const userName = computed(() => userStore.userInfo.name || "管理员");

const now = new Date();
const currentDate = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`;
const hour = now.getHours();
const greeting = hour < 12 ? "上午好" : hour < 18 ? "下午好" : "晚上好";

const cards = ref([
  { title: "总订单数", value: "0", icon: ShoppingCart, color: "#409EFF", extra: "" },
  { title: "待发货", value: "0", icon: TrendCharts, color: "#e6a23c", extra: "需及时处理" },
  { title: "总销售额", value: "¥0", icon: Goods, color: "#67c23a", extra: "" },
  { title: "用户总数", value: "0", icon: User, color: "#f56c6c", extra: "" }
]);
const recentOrders = ref<any[]>([]);
const hotProducts = ref<any[]>([]);
const orderDist = ref<any[]>([]);

const statusText = (s: number) => ["待付款", "待发货", "已发货", "已完成", "已取消", "已退款"][s] || "未知";
const statusType = (s: number) => (["warning", "info", "", "success", "danger", "danger"] as const)[s] || "info";

onMounted(async () => {
  // Stats
  try {
    const { data: stats } = await http.get<any>("/api/admin/statistics", {}, { loading: false });
    const { data: userStats } = await http.get<any>("/api/admin/statistics/users", {}, { loading: false });
    const totalOrders = stats.totalOrders || 0;
    cards.value[0].value = String(totalOrders);
    cards.value[0].extra = `已完成 ${stats.completedOrders || 0} 单`;
    cards.value[1].value = String(stats.pendingShipment || 0);
    cards.value[2].value = "¥ " + (stats.totalRevenue || 0);
    cards.value[3].value = String(userStats.totalUsers || 0);

    // Order distribution
    const pending = stats.pendingShipment || 0;
    const completed = stats.completedOrders || 0;
    const other = Math.max(0, totalOrders - pending - completed);
    if (totalOrders > 0) {
      orderDist.value = [
        { label: "已完成", count: completed, pct: Math.round((completed / totalOrders) * 100), color: "#67c23a" },
        { label: "待发货", count: pending, pct: Math.round((pending / totalOrders) * 100), color: "#e6a23c" },
        { label: "其他", count: other, pct: Math.round((other / totalOrders) * 100), color: "#909399" }
      ];
    }
  } catch (e) {
    /* ignore */
  }

  // Recent orders
  try {
    const { data } = await http.get<any>("/api/admin/orders", { page: 1, size: 10 }, { loading: false });
    recentOrders.value = data.records || [];
  } catch (e) {
    /* ignore */
  }

  // Hot products
  try {
    const { data } = await http.get<any>("/api/products/hot", { size: 5 }, { loading: false });
    hotProducts.value = data.records || [];
  } catch (e) {
    /* ignore */
  }
});
</script>

<style scoped>
.home-container {
  padding: 10px;
}

.welcome-card {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
}
.welcome-card :deep(.el-card__body) {
  padding: 24px 28px;
}
.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.welcome-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
}
.welcome-date {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
}

/* Stat Card */
.stat-card {
  border-radius: 10px;
}
.stat-card :deep(.el-card__body) {
  min-height: 100px;
}
.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
}
.stat-extra {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
}
.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Card Header */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
}

/* Quick Actions */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 10px;
  background: #f9fafb;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: #606266;
}
.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

/* Hot Products */
.hot-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}
.hot-product-item:last-child {
  border-bottom: none;
}
.hot-rank {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: #c0c4cc;
  flex-shrink: 0;
}
.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffa500);
}
.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
}
.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #b87333);
}
.hot-info {
  flex: 1;
  min-width: 0;
}
.hot-name {
  display: block;
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hot-brand {
  font-size: 11px;
  color: #909399;
}
.hot-sales {
  text-align: right;
  flex-shrink: 0;
}
.hot-sales-num {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
}
.hot-sales-label {
  font-size: 11px;
  color: #c0c4cc;
  margin-left: 2px;
}

/* Order Distribution */
.order-dist {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.dist-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.dist-bar {
  height: 8px;
  border-radius: 4px;
  background: #f0f2f5;
  overflow: hidden;
}
.dist-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}
.dist-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
.dist-label {
  display: flex;
  align-items: center;
  gap: 4px;
}
.dist-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.dist-count {
  font-weight: 500;
  color: #606266;
}
</style>
