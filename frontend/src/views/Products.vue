<template>
  <div>
    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
      <Search :size="22" color="#FF6B9D" />
      <h2>全部商品</h2>
    </div>
    <n-card style="margin-bottom: 20px; border-radius: 12px;">
      <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
        <n-input v-model:value="keyword" placeholder="搜索商品" clearable style="max-width: 240px;" @keyup.enter="search">
          <template #prefix><Search :size="16" color="#999" /></template>
        </n-input>
        <n-select v-model:value="categoryId" :options="categoryOptions" placeholder="分类" clearable style="width: 160px;" />
        <n-select v-model:value="sortBy" :options="sortOptions" placeholder="排序" clearable style="width: 160px;" />
        <n-button type="primary" @click="search">搜索</n-button>
      </div>
    </n-card>

    <n-spin :show="loading">
      <n-grid :x-gap="16" :y-gap="16" :cols="4" v-if="products.length">
        <n-gi v-for="p in products" :key="p.id">
          <n-card hoverable style="border-radius: 12px; cursor: pointer;" @click="$router.push('/product/' + p.id)">
            <div style="height: 180px; background: #fef5f0; border-radius: 8px; display: flex; align-items: center; justify-content: center; overflow: hidden;">
              <img v-if="p.mainImage" :src="p.mainImage" style="width: 100%; height: 100%; object-fit: cover;" />
              <Package v-else :size="48" color="#ffb6c1" :stroke-width="1.2" />
            </div>
            <div style="margin-top: 12px;">
              <n-ellipsis :line-clamp="2" style="font-size: 14px; font-weight: 500;">{{ p.name }}</n-ellipsis>
              <div style="display: flex; align-items: baseline; gap: 8px; margin-top: 8px;">
                <span style="font-size: 20px; font-weight: 700; color: #FF6B9D;">¥{{ p.price }}</span>
                <span v-if="p.originalPrice" style="font-size: 12px; color: #999; text-decoration: line-through;">¥{{ p.originalPrice }}</span>
              </div>
              <div style="display: flex; justify-content: space-between; margin-top: 4px; font-size: 12px; color: #999;">
                <span>{{ p.brand }}</span>
                <span>已售 {{ p.sales || 0 }}</span>
              </div>
            </div>
          </n-card>
        </n-gi>
      </n-grid>
      <n-empty v-else description="暂无商品" style="margin-top: 80px;">
        <template #icon><Package :size="48" color="#ddd" /></template>
      </n-empty>
    </n-spin>

    <div style="display: flex; justify-content: center; margin-top: 24px;" v-if="total > pageSize">
      <n-pagination v-model:page="page" :page-count="Math.ceil(total / pageSize)" @update:page="fetchProducts" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Package } from 'lucide-vue-next'
import http from '@/utils/http'

const route = useRoute()
const loading = ref(false)
const products = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const keyword = ref('')
const categoryId = ref(null)
const sortBy = ref(null)
const categories = ref<any[]>([])

const categoryOptions = ref<any[]>([])
const sortOptions = [
  { label: '价格升序', value: 'price-asc' },
  { label: '价格降序', value: 'price-desc' },
  { label: '销量降序', value: 'sales-desc' }
]

const fetchProducts = async () => {
  loading.value = true
  const params: any = { page: page.value, size: pageSize }
  if (keyword.value) params.keyword = keyword.value
  if (categoryId.value) params.categoryId = categoryId.value
  if (sortBy.value) {
    const [field, order] = (sortBy.value as string).split('-')
    params.sortBy = field
    params.sortOrder = order
  }
  try {
    const r: any = await http.get('/products', { params })
    products.value = r.data?.records || []
    total.value = r.data?.total || 0
  } finally { loading.value = false }
}

const search = () => { page.value = 1; fetchProducts() }

onMounted(async () => {
  if (route.query.categoryId) categoryId.value = Number(route.query.categoryId) as any
  if (route.query.keyword) keyword.value = route.query.keyword as string
  try {
    const r: any = await http.get('/categories')
    categories.value = r.data || []
    categoryOptions.value = categories.value.map((c: any) => ({ label: c.name, value: c.id }))
  } catch {}
  fetchProducts()
})

watch(() => route.query, () => {
  if (route.query.categoryId) categoryId.value = Number(route.query.categoryId) as any
  if (route.query.keyword) keyword.value = route.query.keyword as string
  fetchProducts()
})
</script>
