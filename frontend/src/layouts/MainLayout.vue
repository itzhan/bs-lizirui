<template>
  <n-layout style="min-height: 100vh;">
    <!-- Header -->
    <n-layout-header bordered style="height: 64px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between; background: #fff; position: sticky; top: 0; z-index: 100;">
      <div style="display: flex; align-items: center; gap: 32px;">
        <router-link to="/" style="display: flex; align-items: center; gap: 8px;">
          <n-icon size="28" color="#FF6B9D"><Baby /></n-icon>
          <span style="font-size: 20px; font-weight: 700; color: #FF6B9D;">母婴乐园</span>
        </router-link>
        <n-menu mode="horizontal" :value="activeMenu" :options="menuOptions" />
      </div>
      <div style="display: flex; align-items: center; gap: 12px;">
        <!-- Search Box -->
        <n-input v-model:value="searchKeyword" placeholder="搜索商品..." size="small" round clearable style="width: 200px;" @keyup.enter="handleSearch">
          <template #prefix><n-icon size="16"><SearchOutline /></n-icon></template>
        </n-input>
        <router-link to="/cart">
          <n-badge :value="cartStore.totalCount" :max="99">
            <n-button quaternary circle><n-icon size="22"><CartOutline /></n-icon></n-button>
          </n-badge>
        </router-link>
        <template v-if="userStore.isLoggedIn">
          <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
            <n-button quaternary>
              <n-icon size="18"><PersonOutline /></n-icon>
              <span style="margin-left: 6px;">{{ userStore.nickname || '用户' }}</span>
            </n-button>
          </n-dropdown>
        </template>
        <template v-else>
          <router-link to="/login"><n-button type="primary" size="small">登录</n-button></router-link>
          <router-link to="/register"><n-button size="small">注册</n-button></router-link>
        </template>
      </div>
    </n-layout-header>

    <!-- Content -->
    <n-layout-content style="padding: 24px; max-width: 1200px; margin: 0 auto; width: 100%; min-height: calc(100vh - 64px - 200px);">
      <router-view />
    </n-layout-content>

    <!-- Rich Footer -->
    <n-layout-footer style="background: #2c2c2c; color: #ccc; padding: 0;">
      <div style="max-width: 1200px; margin: 0 auto; padding: 40px 24px 20px;">
        <div style="display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr; gap: 32px; margin-bottom: 24px;">
          <!-- About -->
          <div>
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 12px;">
              <n-icon size="24" color="#FF6B9D"><Baby /></n-icon>
              <span style="font-size: 18px; font-weight: 700; color: #fff;">母婴乐园</span>
            </div>
            <p style="font-size: 13px; line-height: 1.8; color: #999;">
              母婴乐园是专业的一站式母婴购物平台，精选全球优质母婴品牌，包含奶粉辅食、纸尿裤、洗护用品、服装鞋帽、玩具、出行工具等品类，为宝宝和妈妈提供安全放心的购物体验。
            </p>
          </div>
          <!-- Quick Links -->
          <div>
            <h4 style="color: #fff; margin-bottom: 12px; font-size: 15px;">快速链接</h4>
            <div style="display: flex; flex-direction: column; gap: 8px;">
              <router-link to="/" class="footer-link">首页</router-link>
              <router-link to="/products" class="footer-link">全部商品</router-link>
              <router-link to="/cart" class="footer-link">购物车</router-link>
              <router-link to="/orders" class="footer-link">我的订单</router-link>
            </div>
          </div>
          <!-- Customer Service -->
          <div>
            <h4 style="color: #fff; margin-bottom: 12px; font-size: 15px;">客户服务</h4>
            <div style="display: flex; flex-direction: column; gap: 8px; font-size: 13px; color: #999;">
              <span>退换货政策：7天无理由退换</span>
              <span>配送说明：全场包邮</span>
              <span>客服电话：400-123-4567</span>
              <span>服务时间：9:00 - 21:00</span>
            </div>
          </div>
          <!-- Contact -->
          <div>
            <h4 style="color: #fff; margin-bottom: 12px; font-size: 15px;">联系我们</h4>
            <div style="display: flex; flex-direction: column; gap: 8px; font-size: 13px; color: #999;">
              <span>邮箱：support@babyshop.com</span>
              <span>地址：广东省深圳市南山区</span>
              <span>工作日：周一至周五</span>
            </div>
          </div>
        </div>
        <div style="border-top: 1px solid #444; padding-top: 16px; text-align: center; font-size: 12px; color: #666;">
          © 2024 母婴乐园商城 — 毕业设计作品 · 技术栈：Spring Boot + Vue 3 + Naive UI
        </div>
      </div>
    </n-layout-footer>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, h } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { NIcon } from 'naive-ui'
import { CartOutline, PersonOutline, HomeOutline, GridOutline, HeartOutline, ListOutline, LocationOutline, SearchOutline } from '@vicons/ionicons5'
import { Baby } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const searchKeyword = ref('')

const activeMenu = computed(() => {
  if (route.path === '/') return 'home'
  if (route.path.startsWith('/products') || route.path.startsWith('/product/')) return 'products'
  return ''
})

const renderIcon = (icon: any) => () => h(NIcon, null, { default: () => h(icon) })

const menuOptions = [
  { label: () => h(RouterLink, { to: '/' }, { default: () => '首页' }), key: 'home', icon: renderIcon(HomeOutline) },
  { label: () => h(RouterLink, { to: '/products' }, { default: () => '全部商品' }), key: 'products', icon: renderIcon(GridOutline) }
]

const userMenuOptions = [
  { label: '我的订单', key: 'orders', icon: renderIcon(ListOutline) },
  { label: '我的收藏', key: 'favorites', icon: renderIcon(HeartOutline) },
  { label: '收货地址', key: 'address', icon: renderIcon(LocationOutline) },
  { label: '个人中心', key: 'profile', icon: renderIcon(PersonOutline) },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' }
]

const handleUserMenu = (key: string) => {
  if (key === 'logout') {
    userStore.logout()
    router.push('/')
  } else {
    router.push('/' + key)
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'products', query: { keyword: searchKeyword.value.trim() } })
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) cartStore.fetchCart()
})
</script>

<style>
.footer-link { color: #999; text-decoration: none; font-size: 13px; transition: color .2s; }
.footer-link:hover { color: #FF6B9D; }
</style>
