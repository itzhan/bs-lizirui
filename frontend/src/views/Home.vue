<template>
  <div>
    <!-- Banner Carousel -->
    <n-carousel autoplay show-arrow style="height: 360px; border-radius: 12px; overflow: hidden; margin-bottom: 32px;">
      <div v-for="b in banners" :key="b.id" class="carousel-item" style="position: relative;">
        <img :src="b.imageUrl || defaultBannerImages[b.id % 4]" style="width: 100%; height: 100%; object-fit: cover;" />
        <div style="position: absolute; bottom: 0; left: 0; right: 0; padding: 32px 60px; background: linear-gradient(transparent, rgba(0,0,0,0.5));">
          <h2 style="font-size: 32px; font-weight: 700; color: #fff; margin-bottom: 8px;">{{ b.title }}</h2>
          <p style="font-size: 16px; color: rgba(255,255,255,0.85);">精选优质母婴用品，呵护宝宝健康成长</p>
        </div>
      </div>
      <div v-if="!banners.length" class="carousel-item" style="background: linear-gradient(135deg, #FF6B9D22, #FFB6C122); display: flex; align-items: center; justify-content: center;">
        <div style="text-align: center;">
          <Baby :size="72" color="#FF6B9D" />
          <h2 style="font-size: 32px; color: #FF6B9D; margin-top: 16px;">欢迎来到母婴乐园</h2>
        </div>
      </div>
    </n-carousel>

    <!-- Announcements -->
    <n-alert v-if="announcement" type="info" :title="announcement.title" style="margin-bottom: 24px;" closable>
      {{ announcement.content }}
    </n-alert>

    <!-- Stats Banner -->
    <div class="stats-banner">
      <div class="stat-item" v-for="s in stats" :key="s.label">
        <component :is="s.icon" :size="28" color="#FF6B9D" />
        <span class="stat-number">{{ s.value }}</span>
        <span class="stat-label">{{ s.label }}</span>
      </div>
    </div>

    <!-- Categories -->
    <div style="margin-bottom: 32px;">
      <div class="section-title"><Tag :size="22" color="#FF6B9D" /><h3>商品分类</h3></div>
      <n-grid :x-gap="12" :y-gap="12" :cols="6">
        <n-gi v-for="c in categories" :key="c.id">
          <div class="category-card" @click="$router.push({ name: 'products', query: { categoryId: c.id } })">
            <n-icon :size="28" :color="categoryColors[c.id % 6]"><component :is="categoryIcons[c.id % 6]" /></n-icon>
            <span style="font-size: 14px; font-weight: 500;">{{ c.name }}</span>
          </div>
        </n-gi>
      </n-grid>
    </div>

    <!-- New Arrivals -->
    <div style="margin-bottom: 32px;">
      <div class="section-title">
        <Sparkles :size="22" color="#FF6B9D" /><h3>新品上架</h3>
        <router-link to="/products" style="margin-left: auto; font-size: 13px; color: #FF6B9D;">查看更多 →</router-link>
      </div>
      <n-grid :x-gap="16" :y-gap="16" :cols="4">
        <n-gi v-for="p in newProducts" :key="p.id">
          <div class="product-card" @click="$router.push('/product/' + p.id)">
            <div class="product-img">
              <img v-if="p.mainImage" :src="p.mainImage" />
              <Package v-else :size="48" color="#ffb6c1" :stroke-width="1.2" />
              <span v-if="getDiscount(p) > 0" class="discount-badge">-{{ getDiscount(p) }}%</span>
            </div>
            <div class="product-info">
              <div class="product-name">{{ p.name }}</div>
              <span v-if="p.brand" class="product-brand">{{ p.brand }}</span>
              <div class="product-price-row">
                <span class="product-price">¥{{ p.price }}</span>
                <span v-if="p.originalPrice && p.originalPrice > p.price" class="product-original">¥{{ p.originalPrice }}</span>
              </div>
              <div class="product-meta">已售 {{ p.sales || 0 }} 件</div>
            </div>
          </div>
        </n-gi>
      </n-grid>
    </div>

    <!-- Recommended Products -->
    <div style="margin-bottom: 32px;">
      <div class="section-title">
        <Star :size="22" color="#FF6B9D" /><h3>精选推荐</h3>
        <router-link to="/products" style="margin-left: auto; font-size: 13px; color: #FF6B9D;">查看更多 →</router-link>
      </div>
      <n-grid :x-gap="16" :y-gap="16" :cols="4">
        <n-gi v-for="p in recommendProducts" :key="p.id">
          <div class="product-card" @click="$router.push('/product/' + p.id)">
            <div class="product-img">
              <img v-if="p.mainImage" :src="p.mainImage" />
              <Package v-else :size="48" color="#ffb6c1" :stroke-width="1.2" />
              <span v-if="getDiscount(p) > 0" class="discount-badge">-{{ getDiscount(p) }}%</span>
            </div>
            <div class="product-info">
              <div class="product-name">{{ p.name }}</div>
              <span v-if="p.brand" class="product-brand">{{ p.brand }}</span>
              <div class="product-price-row">
                <span class="product-price">¥{{ p.price }}</span>
                <span v-if="p.originalPrice && p.originalPrice > p.price" class="product-original">¥{{ p.originalPrice }}</span>
              </div>
              <div class="product-meta">已售 {{ p.sales || 0 }} 件</div>
            </div>
          </div>
        </n-gi>
      </n-grid>
    </div>

    <!-- Hot Products -->
    <div style="margin-bottom: 32px;">
      <div class="section-title"><Flame :size="22" color="#FF6B9D" /><h3>热销榜单</h3></div>
      <n-grid :x-gap="16" :y-gap="16" :cols="4">
        <n-gi v-for="(p, idx) in hotProducts" :key="p.id">
          <div class="product-card" @click="$router.push('/product/' + p.id)">
            <div class="product-img">
              <img v-if="p.mainImage" :src="p.mainImage" />
              <Package v-else :size="48" color="#ffb6c1" :stroke-width="1.2" />
              <span v-if="getDiscount(p) > 0" class="discount-badge">-{{ getDiscount(p) }}%</span>
              <span v-if="idx < 3" class="rank-badge" :class="'rank-' + (idx + 1)">TOP{{ idx + 1 }}</span>
            </div>
            <div class="product-info">
              <div class="product-name">{{ p.name }}</div>
              <span v-if="p.brand" class="product-brand">{{ p.brand }}</span>
              <div class="product-price-row">
                <span class="product-price">¥{{ p.price }}</span>
                <span v-if="p.originalPrice && p.originalPrice > p.price" class="product-original">¥{{ p.originalPrice }}</span>
              </div>
              <div class="product-meta">已售 {{ p.sales || 0 }} 件</div>
            </div>
          </div>
        </n-gi>
      </n-grid>
    </div>

    <!-- Brands -->
    <div style="margin-bottom: 32px;">
      <div class="section-title"><Award :size="22" color="#FF6B9D" /><h3>品牌精选</h3></div>
      <div class="brands-grid">
        <div class="brand-item" v-for="b in brands" :key="b.name" @click="$router.push({ name: 'products', query: { keyword: b.name } })">
          <span class="brand-logo">{{ b.abbr }}</span>
          <span class="brand-name">{{ b.name }}</span>
          <span class="brand-desc">{{ b.desc }}</span>
        </div>
      </div>
    </div>

    <!-- Service Guarantees -->
    <div class="service-bar">
      <div class="service-item"><ShieldCheck :size="20" color="#FF6B9D" /><span>正品保障</span></div>
      <div class="service-item"><Truck :size="20" color="#FF6B9D" /><span>全场包邮</span></div>
      <div class="service-item"><RefreshCw :size="20" color="#FF6B9D" /><span>7天退换</span></div>
      <div class="service-item"><Headphones :size="20" color="#FF6B9D" /><span>专属客服</span></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Baby, Tag, Star, Flame, Package, Gift, Sparkles, Award, ShieldCheck, Truck, RefreshCw, Headphones, ShoppingBag, Milk, Shirt, Bath, Gamepad2, Car } from 'lucide-vue-next'
import http from '@/utils/http'

const defaultBannerImages = [
  'https://picsum.photos/seed/banner-spring/1200/400',
  'https://picsum.photos/seed/banner-new/1200/400',
  'https://picsum.photos/seed/banner-diaper/1200/400',
  'https://picsum.photos/seed/banner-618/1200/400'
]

const categoryIcons = [ShoppingBag, Milk, Bath, Shirt, Gamepad2, Car]
const categoryColors = ['#FF6B9D', '#FF9A56', '#67C23A', '#409EFF', '#E6A23C', '#F56C6C']

const brands = [
  { name: '飞鹤', abbr: '飞', desc: '国产高端奶粉' },
  { name: '爱他美', abbr: '爱', desc: '德国品质奶粉' },
  { name: '花王', abbr: '花', desc: '日本纸尿裤品牌' },
  { name: '贝亲', abbr: '贝', desc: '母婴用品专家' },
  { name: '好孩子', abbr: '好', desc: '婴儿出行首选' },
  { name: '乐高', abbr: '乐', desc: '益智玩具品牌' }
]

const stats = ref([
  { label: '品牌入驻', value: '30+', icon: Award },
  { label: '精选商品', value: '32+', icon: Package },
  { label: '用户好评', value: '65+', icon: Star },
  { label: '售后保障', value: '7天', icon: ShieldCheck }
])

const banners = ref<any[]>([])
const categories = ref<any[]>([])
const recommendProducts = ref<any[]>([])
const hotProducts = ref<any[]>([])
const newProducts = ref<any[]>([])
const announcement = ref<any>(null)

const getDiscount = (p: any) => {
  if (!p.originalPrice || p.originalPrice <= p.price) return 0
  return Math.round((1 - p.price / p.originalPrice) * 100)
}

onMounted(async () => {
  try { const r: any = await http.get('/banners'); banners.value = r.data || [] } catch {}
  try { const r: any = await http.get('/categories'); categories.value = (r.data || []).filter((c: any) => c.parentId === 0) } catch {}
  try { const r: any = await http.get('/recommend/cf', { params: { size: 8 } }); recommendProducts.value = r.data || [] } catch {}
  try { const r: any = await http.get('/products/hot', { params: { size: 8 } }); hotProducts.value = r.data?.records || [] } catch {}
  try { const r: any = await http.get('/products', { params: { page: 1, size: 4, sortBy: 'id', sortOrder: 'desc' } }); newProducts.value = r.data?.records || [] } catch {}
  try { const r: any = await http.get('/announcements', { params: { size: 1 } }); announcement.value = r.data?.records?.[0] || null } catch {}
})
</script>

<style scoped>
.carousel-item { height: 360px; width: 100%; }
.section-title { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.section-title h3 { font-size: 20px; font-weight: 600; margin: 0; }

/* Stats Banner */
.stats-banner { display: flex; justify-content: space-around; background: linear-gradient(135deg, #fff5f7, #fff0ed); border-radius: 12px; padding: 24px; margin-bottom: 32px; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.stat-number { font-size: 24px; font-weight: 700; color: #333; }
.stat-label { font-size: 13px; color: #999; }

/* Category Cards */
.category-card { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 20px 12px; background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; cursor: pointer; transition: all .2s; }
.category-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(255,107,157,.15); border-color: #FF6B9D44; }

/* Product Card */
.product-card { background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; cursor: pointer; transition: all .2s; overflow: hidden; }
.product-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,.08); }
.product-img { height: 180px; background: #fef5f0; display: flex; align-items: center; justify-content: center; overflow: hidden; position: relative; }
.product-img img { width: 100%; height: 100%; object-fit: cover; }
.discount-badge { position: absolute; top: 8px; left: 8px; background: #FF6B9D; color: #fff; font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 4px; }
.rank-badge { position: absolute; top: 8px; right: 8px; color: #fff; font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 4px; }
.rank-1 { background: linear-gradient(135deg, #FFD700, #FFA500); }
.rank-2 { background: linear-gradient(135deg, #C0C0C0, #A0A0A0); }
.rank-3 { background: linear-gradient(135deg, #CD7F32, #B87333); }
.product-info { padding: 12px; }
.product-name { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-brand { display: inline-block; font-size: 11px; color: #409EFF; background: #409EFF15; padding: 1px 6px; border-radius: 3px; margin-top: 4px; }
.product-price-row { display: flex; align-items: baseline; gap: 6px; margin-top: 8px; }
.product-price { font-size: 20px; font-weight: 700; color: #FF6B9D; }
.product-original { font-size: 12px; color: #999; text-decoration: line-through; }
.product-meta { font-size: 12px; color: #999; margin-top: 4px; }

/* Brands */
.brands-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.brand-item { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 20px 12px; background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; cursor: pointer; transition: all .2s; }
.brand-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(255,107,157,.12); }
.brand-logo { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #FF6B9D22, #FF9A5622); display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: 700; color: #FF6B9D; }
.brand-name { font-size: 14px; font-weight: 600; color: #333; }
.brand-desc { font-size: 11px; color: #999; }

/* Service Bar */
.service-bar { display: flex; justify-content: space-around; background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; padding: 20px; margin-bottom: 16px; }
.service-item { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #666; }
</style>
