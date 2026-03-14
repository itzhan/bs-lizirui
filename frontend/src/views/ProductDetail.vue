<template>
  <div v-if="product" style="max-width: 1000px; margin: 0 auto;">
    <!-- Breadcrumb -->
    <n-breadcrumb style="margin-bottom: 16px;">
      <n-breadcrumb-item @click="$router.push('/')">首页</n-breadcrumb-item>
      <n-breadcrumb-item @click="$router.push('/products')">全部商品</n-breadcrumb-item>
      <n-breadcrumb-item>{{ product.name }}</n-breadcrumb-item>
    </n-breadcrumb>

    <!-- Main Info -->
    <n-card style="border-radius: 12px; margin-bottom: 24px;">
      <n-grid :x-gap="32" :cols="2">
        <n-gi>
          <div style="height: 400px; background: #fef5f0; border-radius: 12px; display: flex; align-items: center; justify-content: center; overflow: hidden;">
            <img v-if="product.mainImage" :src="product.mainImage" style="width: 100%; height: 100%; object-fit: cover;" />
            <Package v-else :size="80" color="#ffb6c1" :stroke-width="1" />
          </div>
        </n-gi>
        <n-gi>
          <h1 style="font-size: 24px; font-weight: 700; margin-bottom: 8px; line-height: 1.4;">{{ product.name }}</h1>
          <n-space size="small" style="margin-bottom: 12px;">
            <n-tag v-if="product.brand" size="small" type="info">{{ product.brand }}</n-tag>
            <n-tag v-if="product.recommend" size="small" type="success">推荐</n-tag>
            <n-tag v-if="discount > 0" size="small" type="error">省 ¥{{ savedAmount }}</n-tag>
          </n-space>

          <!-- Price Section -->
          <div style="background: linear-gradient(135deg, #FFF0F5, #FFF5EE); border-radius: 10px; padding: 16px 20px; margin-bottom: 16px;">
            <div style="display: flex; align-items: baseline; gap: 12px;">
              <span style="font-size: 14px; color: #FF6B9D;">¥</span>
              <span style="font-size: 36px; font-weight: 700; color: #FF6B9D;">{{ product.price }}</span>
              <span v-if="product.originalPrice && product.originalPrice > product.price" style="font-size: 16px; color: #999; text-decoration: line-through;">¥{{ product.originalPrice }}</span>
              <n-tag v-if="discount > 0" type="error" size="small" round>{{ discount }}折</n-tag>
            </div>
            <div style="font-size: 12px; color: #999; margin-top: 4px;">
              累计销量 {{ product.sales || 0 }} 件 · {{ reviewSummary.count }} 条评价 · {{ reviewSummary.avg }} 分
            </div>
          </div>

          <!-- Specs Quick View -->
          <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; margin-bottom: 16px; font-size: 14px;">
            <div style="display:flex;align-items:center;gap:6px;color:#666"><Tag :size="14" color="#ccc" /> 分类：{{ product.categoryName || '—' }}</div>
            <div style="display:flex;align-items:center;gap:6px;color:#666"><Award :size="14" color="#ccc" /> 品牌：{{ product.brand || '—' }}</div>
            <div style="display:flex;align-items:center;gap:6px;color:#666"><WarehouseIcon :size="14" color="#ccc" /> 库存：{{ product.stock }} 件</div>
            <div style="display:flex;align-items:center;gap:6px;color:#666"><TrendingUp :size="14" color="#ccc" /> 销量：{{ product.sales || 0 }} 件</div>
          </div>

          <p style="color: #666; line-height: 1.8; margin-bottom: 20px; font-size: 14px;">{{ product.description }}</p>

          <!-- Actions -->
          <div style="display: flex; gap: 12px;">
            <n-input-number v-model:value="quantity" :min="1" :max="product.stock" size="large" style="width: 140px;" />
            <n-button type="primary" size="large" @click="addToCart" style="flex:1;">
              <template #icon><ShoppingCart :size="18" /></template>
              加入购物车
            </n-button>
            <n-button :type="isFavorite ? 'error' : 'default'" size="large" @click="toggleFavorite">
              <template #icon><Heart :size="18" :fill="isFavorite ? '#FF6B9D' : 'none'" /></template>
              {{ isFavorite ? '已收藏' : '收藏' }}
            </n-button>
          </div>

          <!-- Service Guarantees -->
          <div style="display: flex; gap: 20px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0;">
            <span style="display:flex;align-items:center;gap:4px;font-size:12px;color:#999;"><ShieldCheck :size="14" color="#67C23A" /> 正品保障</span>
            <span style="display:flex;align-items:center;gap:4px;font-size:12px;color:#999;"><Truck :size="14" color="#67C23A" /> 全场包邮</span>
            <span style="display:flex;align-items:center;gap:4px;font-size:12px;color:#999;"><RefreshCw :size="14" color="#67C23A" /> 7天退换</span>
          </div>
        </n-gi>
      </n-grid>
    </n-card>

    <!-- Reviews Section -->
    <n-card style="border-radius: 12px; margin-bottom: 24px;">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <MessageSquare :size="20" color="#FF6B9D" />
            <span style="font-size: 16px; font-weight: 600;">用户评价 ({{ reviewSummary.count }})</span>
          </div>
          <div style="display: flex; align-items: center; gap: 4px;">
            <n-rate :value="reviewSummary.avg" readonly size="small" allow-half />
            <span style="font-size: 14px; color: #FF6B9D; font-weight: 600;">{{ reviewSummary.avg }}</span>
          </div>
        </div>
      </template>
      <div v-for="r in reviews" :key="r.id" style="padding: 12px 0; border-bottom: 1px solid #f5f5f5;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <n-avatar :size="28" round style="background: linear-gradient(135deg, #FF6B9D, #FF9A56); color: #fff; font-size: 12px;">{{ (r.nickname || '匿')[0] }}</n-avatar>
            <span style="font-weight: 500; font-size: 14px;">{{ r.nickname || '匿名用户' }}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <n-rate :value="r.rating" readonly size="small" />
            <span style="font-size: 12px; color: #999;">{{ r.createdAt }}</span>
          </div>
        </div>
        <p style="color: #666; font-size: 14px; line-height: 1.6; margin: 0;">{{ r.content }}</p>
      </div>
      <n-empty v-if="!reviews.length" description="暂无评价" style="padding: 40px 0;" />
    </n-card>

    <!-- Related Products -->
    <div v-if="relatedProducts.length">
      <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px;">
        <Sparkles :size="20" color="#FF6B9D" />
        <h3 style="font-size: 16px; font-weight: 600; margin: 0;">相关推荐</h3>
      </div>
      <n-grid :x-gap="16" :y-gap="16" :cols="5">
        <n-gi v-for="rp in relatedProducts" :key="rp.id">
          <n-card hoverable size="small" style="border-radius: 10px; cursor: pointer;" @click="goToProduct(rp.id)">
            <div style="height: 120px; background: #fef5f0; border-radius: 6px; display: flex; align-items: center; justify-content: center; overflow: hidden;">
              <img v-if="rp.mainImage" :src="rp.mainImage" style="width:100%;height:100%;object-fit:cover;" />
              <Package v-else :size="32" color="#ffb6c1" />
            </div>
            <n-ellipsis :line-clamp="1" style="font-size: 13px; margin-top: 8px;">{{ rp.name }}</n-ellipsis>
            <span style="color: #FF6B9D; font-weight: 700; font-size: 14px;">¥{{ rp.price }}</span>
          </n-card>
        </n-gi>
      </n-grid>
    </div>
  </div>
  <div v-else style="display: flex; justify-content: center; padding: 100px;">
    <n-spin size="large" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { Package, ShoppingCart, Heart, MessageSquare, Sparkles, Tag, Award, TrendingUp, ShieldCheck, Truck, RefreshCw, Warehouse as WarehouseIcon, User } from 'lucide-vue-next'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import http from '@/utils/http'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const cartStore = useCartStore()
const userStore = useUserStore()
const product = ref<any>(null)
const quantity = ref(1)
const isFavorite = ref(false)
const reviews = ref<any[]>([])
const relatedProducts = ref<any[]>([])

const id = ref(Number(route.params.id))

const discount = computed(() => {
  if (!product.value || !product.value.originalPrice || product.value.originalPrice <= product.value.price) return 0
  return Math.round((product.value.price / product.value.originalPrice) * 10)
})
const savedAmount = computed(() => {
  if (!product.value || !product.value.originalPrice) return 0
  return (product.value.originalPrice - product.value.price).toFixed(0)
})
const reviewSummary = computed(() => {
  if (!reviews.value.length) return { count: 0, avg: 0 }
  const avg = reviews.value.reduce((s: number, r: any) => s + r.rating, 0) / reviews.value.length
  return { count: reviews.value.length, avg: Math.round(avg * 10) / 10 }
})

const loadProduct = async () => {
  product.value = null
  try { const r: any = await http.get(`/products/${id.value}`); product.value = r.data } catch {}
  try { const r: any = await http.get(`/reviews/product/${id.value}`, { params: { size: 20 } }); reviews.value = r.data?.records || [] } catch {}
  if (userStore.isLoggedIn) {
    try { const r: any = await http.get(`/favorites/check/${id.value}`); isFavorite.value = r.data === true } catch {}
  }
  // Load related products by same category
  if (product.value?.categoryId) {
    try {
      const r: any = await http.get('/products', { params: { categoryId: product.value.categoryId, size: 5 } })
      relatedProducts.value = (r.data?.records || []).filter((p: any) => p.id !== id.value).slice(0, 5)
    } catch {}
  }
}

const goToProduct = (pid: number) => {
  id.value = pid
  router.push('/product/' + pid)
  loadProduct()
  window.scrollTo(0, 0)
}

onMounted(loadProduct)

const addToCart = async () => {
  if (!userStore.isLoggedIn) { message.warning('请先登录'); return }
  try { await cartStore.addToCart(id.value, quantity.value); message.success('已加入购物车') } catch (e: any) { message.error(e.message || '操作失败') }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) { message.warning('请先登录'); return }
  try {
    if (isFavorite.value) { await http.delete(`/favorites/${id.value}`); isFavorite.value = false; message.success('已取消收藏') }
    else { await http.post(`/favorites/${id.value}`); isFavorite.value = true; message.success('已收藏') }
  } catch (e: any) { message.error(e.message || '操作失败') }
}
</script>
