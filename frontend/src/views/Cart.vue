<template>
  <div>
    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px;">
      <ShoppingCart :size="24" color="#FF6B9D" />
      <h2>购物车</h2>
    </div>
    <n-spin :show="loading">
      <div v-if="cartStore.items.length">
        <n-card v-for="item in cartStore.items" :key="item.id" style="margin-bottom: 12px; border-radius: 12px;">
          <div style="display: flex; align-items: center; gap: 16px;">
            <n-checkbox :checked="item.checked === 1" @update:checked="(v: boolean) => cartStore.toggleChecked(item.id, v ? 1 : 0)" />
            <div style="width: 80px; height: 80px; background: #fef5f0; border-radius: 8px; display: flex; align-items: center; justify-content: center; overflow: hidden; flex-shrink: 0;">
              <img v-if="item.mainImage" :src="item.mainImage" style="width: 100%; height: 100%; object-fit: cover;" />
              <Package v-else :size="32" color="#ffb6c1" :stroke-width="1.2" />
            </div>
            <div style="flex: 1; min-width: 0;">
              <n-ellipsis :line-clamp="1" style="font-weight: 500;">{{ item.productName || '商品' }}</n-ellipsis>
              <span style="color: #FF6B9D; font-weight: 700; font-size: 18px;">¥{{ item.price }}</span>
            </div>
            <n-input-number v-model:value="item.quantity" :min="1" :max="99" size="small" style="width: 120px;"
              @update:value="(v: number) => cartStore.updateQuantity(item.id, v)" />
            <n-popconfirm @positive-click="cartStore.removeItem(item.id)">
              <template #trigger><n-button text type="error"><Trash2 :size="18" /></n-button></template>
              确认删除？
            </n-popconfirm>
          </div>
        </n-card>

        <n-card style="border-radius: 12px; margin-top: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>已选 {{ cartStore.items.filter(i => i.checked).length }} 件商品</span>
            <div style="display: flex; align-items: center; gap: 16px;">
              <span style="font-size: 24px; font-weight: 700; color: #FF6B9D;">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
              <n-button type="primary" size="large" :disabled="cartStore.totalPrice === 0" @click="showCheckout = true">去结算</n-button>
            </div>
          </div>
        </n-card>
      </div>
      <n-empty v-else :description="'购物车是空的'" style="margin-top: 80px;">
        <template #icon><ShoppingCart :size="48" color="#ddd" /></template>
      </n-empty>
    </n-spin>

    <!-- Checkout Modal -->
    <n-modal v-model:show="showCheckout" preset="card" title="确认下单" style="width: 500px;">
      <n-form-item label="选择收货地址">
        <n-select v-model:value="selectedAddress" :options="addressOptions" placeholder="请选择收货地址" />
      </n-form-item>
      <n-form-item label="备注"><n-input v-model:value="remark" type="textarea" placeholder="可选" /></n-form-item>
      <template #footer>
        <n-button type="primary" :loading="submitting" @click="submitOrder">提交订单</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { ShoppingCart, Package, Trash2 } from 'lucide-vue-next'
import { useCartStore } from '@/stores/cart'
import http from '@/utils/http'

const router = useRouter()
const message = useMessage()
const cartStore = useCartStore()
const loading = ref(false)
const showCheckout = ref(false)
const submitting = ref(false)
const selectedAddress = ref(null)
const remark = ref('')
const addresses = ref<any[]>([])

const addressOptions = computed(() => addresses.value.map((a: any) => ({
  label: `${a.receiver} ${a.phone} - ${a.province}${a.city}${a.district} ${a.detail}`,
  value: a.id
})))

onMounted(async () => {
  loading.value = true
  try { await cartStore.fetchCart() } finally { loading.value = false }
  try { const r: any = await http.get('/addresses'); addresses.value = r.data || [] } catch {}
})

const submitOrder = async () => {
  if (!selectedAddress.value) { message.warning('请选择收货地址'); return }
  const checkedIds = cartStore.items.filter(i => i.checked).map(i => i.id)
  if (!checkedIds.length) { message.warning('请勾选商品'); return }
  submitting.value = true
  try {
    await http.post('/orders', { addressId: selectedAddress.value, remark: remark.value, cartIds: checkedIds })
    message.success('下单成功')
    showCheckout.value = false
    await cartStore.fetchCart()
    router.push('/orders')
  } catch (e: any) { message.error(e.message || '下单失败') }
  finally { submitting.value = false }
}
</script>
