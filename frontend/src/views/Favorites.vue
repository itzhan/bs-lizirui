<template>
  <div>
    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
      <Heart :size="24" color="#FF6B9D" />
      <h2>我的收藏</h2>
    </div>
    <n-spin :show="loading">
      <n-grid :x-gap="16" :y-gap="16" :cols="4" v-if="items.length">
        <n-gi v-for="item in items" :key="item.id">
          <n-card hoverable style="border-radius: 12px; cursor: pointer;" @click="$router.push('/product/' + item.productId)">
            <div style="height: 160px; background: #fef0f5; border-radius: 8px; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden;">
              <img v-if="item.mainImage" :src="item.mainImage" style="width: 100%; height: 100%; object-fit: cover;" />
              <Package v-else :size="48" color="#ffb6c1" :stroke-width="1.2" />
              <n-button circle size="small" type="error" style="position: absolute; top: 8px; right: 8px;" @click.stop="removeFav(item.productId)">
                <template #icon><X :size="14" /></template>
              </n-button>
            </div>
            <div style="margin-top: 8px;">
              <n-ellipsis :line-clamp="1" style="font-weight: 500;">{{ item.productName || '商品' }}</n-ellipsis>
              <span style="color: #FF6B9D; font-weight: 700;">¥{{ item.price || '—' }}</span>
            </div>
          </n-card>
        </n-gi>
      </n-grid>
      <n-empty v-else description="暂无收藏" style="margin-top: 80px;">
        <template #icon><Heart :size="48" color="#ddd" /></template>
      </n-empty>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { Heart, Package, X } from 'lucide-vue-next'
import http from '@/utils/http'

const message = useMessage()
const loading = ref(false)
const items = ref<any[]>([])

const fetchFavorites = async () => {
  loading.value = true
  try { const r: any = await http.get('/favorites', { params: { size: 50 } }); items.value = r.data?.records || [] }
  finally { loading.value = false }
}

const removeFav = async (productId: number) => {
  await http.delete(`/favorites/${productId}`)
  message.success('已取消收藏')
  fetchFavorites()
}

onMounted(fetchFavorites)
</script>
