import { defineStore } from 'pinia'
import http from '@/utils/http'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as any[]
  }),
  getters: {
    totalCount: (state) => state.items.reduce((sum, i) => sum + i.quantity, 0),
    checkedItems: (state) => state.items.filter(i => i.checked),
    totalPrice: (state) => state.items.filter(i => i.checked).reduce((sum, i) => sum + (i.price || 0) * i.quantity, 0)
  },
  actions: {
    async fetchCart() {
      try {
        const res: any = await http.get('/cart')
        this.items = res.data || []
      } catch { this.items = [] }
    },
    async addToCart(productId: number, quantity: number = 1) {
      await http.post('/cart', { productId, quantity })
      await this.fetchCart()
    },
    async updateQuantity(cartId: number, quantity: number) {
      await http.put(`/cart/${cartId}/quantity`, { quantity })
      await this.fetchCart()
    },
    async removeItem(cartId: number) {
      await http.delete(`/cart/${cartId}`)
      await this.fetchCart()
    },
    async toggleChecked(cartId: number, checked: number) {
      await http.put(`/cart/${cartId}/checked`, { checked })
      await this.fetchCart()
    }
  }
})
