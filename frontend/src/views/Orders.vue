<template>
  <div>
    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
      <ClipboardList :size="24" color="#FF6B9D" />
      <h2 style="margin: 0;">我的订单</h2>
    </div>

    <!-- Order Stats -->
    <div style="display: flex; gap: 12px; margin-bottom: 20px;">
      <div v-for="s in orderStats" :key="s.label" class="order-stat-card" :class="{ active: activeTab === s.key }" @click="activeTab = s.key; changeTab()">
        <span class="order-stat-num">{{ s.count }}</span>
        <span class="order-stat-label">{{ s.label }}</span>
      </div>
    </div>

    <n-tabs type="line" v-model:value="activeTab" @update:value="changeTab">
      <n-tab-pane name="all" tab="全部" /><n-tab-pane name="0" tab="待付款" /><n-tab-pane name="1" tab="待发货" />
      <n-tab-pane name="2" tab="已发货" /><n-tab-pane name="3" tab="已完成" /><n-tab-pane name="4" tab="已取消" />
    </n-tabs>

    <n-spin :show="loading">
      <div v-if="orders.length">
        <n-card v-for="o in orders" :key="o.id" style="margin-top: 16px; border-radius: 12px;">
          <!-- Order Header -->
          <div style="display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #f5f5f5;">
            <div style="display: flex; align-items: center; gap: 12px;">
              <span style="color: #999; font-size: 13px;">订单号：{{ o.orderNo }}</span>
              <span style="color: #999; font-size: 12px;">{{ o.createdAt }}</span>
            </div>
            <n-tag :type="statusType(o.status)" size="small">{{ statusText(o.status) }}</n-tag>
          </div>

          <!-- Order Items -->
          <div v-if="o._items && o._items.length" style="padding: 12px 0;">
            <div v-for="item in o._items" :key="item.id" style="display: flex; align-items: center; gap: 12px; padding: 8px 0; cursor: pointer;" @click="$router.push('/product/' + item.productId)">
              <div style="width: 60px; height: 60px; background: #fef5f0; border-radius: 8px; display: flex; align-items: center; justify-content: center; overflow: hidden; flex-shrink: 0;">
                <img v-if="item.productImage" :src="item.productImage" style="width: 100%; height: 100%; object-fit: cover;" />
                <Package v-else :size="24" color="#ffb6c1" />
              </div>
              <div style="flex: 1; min-width: 0;">
                <n-ellipsis :line-clamp="1" style="font-size: 14px; font-weight: 500;">{{ item.productName }}</n-ellipsis>
                <span style="font-size: 12px; color: #999;">¥{{ item.price }} × {{ item.quantity }}</span>
              </div>
              <span style="font-size: 14px; font-weight: 600; color: #FF6B9D;">¥{{ item.subtotal }}</span>
            </div>
          </div>
          <div v-else style="padding: 12px 0; display: flex; align-items: center; gap: 8px; color: #999; font-size: 13px;">
            <Package :size="16" color="#ccc" />
            <span>加载商品详情中...</span>
          </div>

          <!-- Order Footer -->
          <div style="display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid #f5f5f5;">
            <div style="font-size: 13px; color: #999;">
              <MapPin :size="14" style="vertical-align: -2px;" /> {{ o.receiver }} {{ o.phone }}
            </div>
            <div style="display: flex; align-items: center; gap: 12px;">
              <span style="font-size: 14px; color: #666;">共 {{ o._items?.length || '—' }} 件，合计：</span>
              <span style="font-size: 20px; font-weight: 700; color: #FF6B9D;">¥{{ o.totalAmount }}</span>
            </div>
          </div>

          <!-- Order Actions -->
          <div v-if="o.status === 0 || o.status === 2 || o.status === 3" style="display: flex; gap: 8px; justify-content: flex-end; margin-top: 12px;">
            <n-button size="small" v-if="o.status === 0" type="primary" @click="payOrder(o.id)">
              <template #icon><CreditCard :size="14" /></template>去支付
            </n-button>
            <n-button size="small" v-if="o.status === 0" @click="cancelOrder(o.id)">取消订单</n-button>
            <n-button size="small" v-if="o.status === 2" type="success" @click="confirmOrder(o.id)">
              <template #icon><CheckCircle :size="14" /></template>确认收货
            </n-button>
            <n-button size="small" v-if="o.status === 3" @click="$router.push('/product/' + (o._items?.[0]?.productId || 1))">
              <template #icon><MessageSquare :size="14" /></template>去评价
            </n-button>
          </div>
        </n-card>
      </div>
      <n-empty v-else description="暂无订单" style="margin-top: 80px;">
        <template #icon><ClipboardList :size="48" color="#ddd" /></template>
        <template #extra>
          <n-button type="primary" @click="$router.push('/products')">去购物</n-button>
        </template>
      </n-empty>
    </n-spin>

    <div style="display: flex; justify-content: center; margin-top: 24px;" v-if="total > 10">
      <n-pagination v-model:page="page" :page-count="Math.ceil(total / 10)" @update:page="fetchOrders" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { ClipboardList, Package, MapPin, CreditCard, CheckCircle, MessageSquare } from 'lucide-vue-next'
import http from '@/utils/http'

const message = useMessage()
const loading = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const activeTab = ref('all')

const orderStats = reactive([
  { label: '待付款', key: '0', count: 0 },
  { label: '待发货', key: '1', count: 0 },
  { label: '已发货', key: '2', count: 0 },
  { label: '已完成', key: '3', count: 0 }
])

const statusText = (s: number) => ['待付款', '待发货', '已发货', '已完成', '已取消', '已退款'][s] || '未知'
const statusType = (s: number) => (['warning', 'info', 'default', 'success', 'error', 'error'] as const)[s] || 'default'

const fetchOrders = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, size: 10 }
    if (activeTab.value !== 'all') params.status = Number(activeTab.value)
    const r: any = await http.get('/orders', { params })
    orders.value = r.data?.records || []
    total.value = r.data?.total || 0

    // Fetch order items for each order
    for (const o of orders.value) {
      try {
        const detail: any = await http.get(`/orders/${o.id}`)
        o._items = detail.data?.items || []
      } catch { o._items = [] }
    }
  } finally { loading.value = false }
}

const changeTab = () => { page.value = 1; fetchOrders() }
const payOrder = async (id: number) => { await http.put(`/orders/${id}/pay`); message.success('支付成功'); fetchOrders() }
const cancelOrder = async (id: number) => { await http.put(`/orders/${id}/cancel`); message.success('已取消'); fetchOrders() }
const confirmOrder = async (id: number) => { await http.put(`/orders/${id}/confirm`); message.success('已确认收货'); fetchOrders() }

const fetchStats = async () => {
  for (const s of orderStats) {
    try {
      const r: any = await http.get('/orders', { params: { status: Number(s.key), size: 1 } })
      s.count = r.data?.total || 0
    } catch { s.count = 0 }
  }
}

onMounted(() => { fetchOrders(); fetchStats() })
</script>

<style scoped>
.order-stat-card { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 16px 12px; background: #fff; border-radius: 10px; border: 1px solid #f0f0f0; cursor: pointer; transition: all .2s; }
.order-stat-card:hover, .order-stat-card.active { border-color: #FF6B9D44; background: #FFF5F7; }
.order-stat-num { font-size: 22px; font-weight: 700; color: #FF6B9D; }
.order-stat-label { font-size: 13px; color: #999; }
</style>
