import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  scrollBehavior() { return { top: 0 } },
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/Home.vue') },
        { path: 'products', name: 'products', component: () => import('@/views/Products.vue') },
        { path: 'product/:id', name: 'productDetail', component: () => import('@/views/ProductDetail.vue') },
        { path: 'cart', name: 'cart', component: () => import('@/views/Cart.vue'), meta: { auth: true } },
        { path: 'orders', name: 'orders', component: () => import('@/views/Orders.vue'), meta: { auth: true } },
        { path: 'favorites', name: 'favorites', component: () => import('@/views/Favorites.vue'), meta: { auth: true } },
        { path: 'profile', name: 'profile', component: () => import('@/views/Profile.vue'), meta: { auth: true } },
        { path: 'address', name: 'address', component: () => import('@/views/Address.vue'), meta: { auth: true } }
      ]
    },
    { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/Register.vue') }
  ]
})

router.beforeEach((to, _from, next) => {
  if (to.meta.auth && !localStorage.getItem('token')) {
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
