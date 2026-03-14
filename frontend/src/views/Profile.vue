<template>
  <div style="max-width: 700px; margin: 0 auto;">
    <!-- User Overview Card -->
    <n-card style="border-radius: 16px; margin-bottom: 24px; background: linear-gradient(135deg, #FFF0F5, #FFF5EE);">
      <div style="display: flex; align-items: center; gap: 20px;">
        <n-avatar :size="72" round style="background: linear-gradient(135deg, #FF6B9D, #FF9A56); font-size: 28px; font-weight: 700; color: #fff;">
          {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U')[0] }}
        </n-avatar>
        <div>
          <h2 style="margin: 0 0 4px 0; font-size: 22px;">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h2>
          <p style="margin: 0; color: #999; font-size: 14px;">用户名：{{ userStore.userInfo?.username }}</p>
          <p style="margin: 0; color: #999; font-size: 13px;">{{ userStore.userInfo?.email || '' }} {{ userStore.userInfo?.phone ? '· ' + userStore.userInfo.phone : '' }}</p>
        </div>
      </div>
    </n-card>

    <!-- Quick Links -->
    <n-grid :x-gap="12" :y-gap="12" :cols="4" style="margin-bottom: 24px;">
      <n-gi>
        <div class="quick-link" @click="$router.push('/orders')">
          <ClipboardList :size="24" color="#FF6B9D" />
          <span class="quick-link-label">我的订单</span>
        </div>
      </n-gi>
      <n-gi>
        <div class="quick-link" @click="$router.push('/favorites')">
          <Heart :size="24" color="#FF6B9D" />
          <span class="quick-link-label">我的收藏</span>
        </div>
      </n-gi>
      <n-gi>
        <div class="quick-link" @click="$router.push('/address')">
          <MapPin :size="24" color="#FF6B9D" />
          <span class="quick-link-label">收货地址</span>
        </div>
      </n-gi>
      <n-gi>
        <div class="quick-link" @click="$router.push('/cart')">
          <ShoppingCart :size="24" color="#FF6B9D" />
          <span class="quick-link-label">购物车</span>
        </div>
      </n-gi>
    </n-grid>

    <!-- Profile Form -->
    <n-card style="border-radius: 12px; margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <UserIcon :size="18" color="#FF6B9D" />
          <span>个人信息</span>
        </div>
      </template>
      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="用户名"><n-input :value="userStore.userInfo?.username" disabled /></n-form-item>
        <n-form-item label="昵称"><n-input v-model:value="form.nickname" /></n-form-item>
        <n-form-item label="手机号"><n-input v-model:value="form.phone" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="form.email" /></n-form-item>
        <n-form-item label="性别">
          <n-radio-group v-model:value="form.gender">
            <n-radio :value="1">男</n-radio><n-radio :value="2">女</n-radio><n-radio :value="0">保密</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-button type="primary" @click="saveProfile" :loading="saving">保存修改</n-button>
      </n-form>
    </n-card>

    <!-- Password -->
    <n-card style="border-radius: 12px;">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <Lock :size="18" color="#FF6B9D" />
          <span>修改密码</span>
        </div>
      </template>
      <n-form :model="pwdForm" label-placement="left" label-width="80">
        <n-form-item label="旧密码"><n-input v-model:value="pwdForm.oldPassword" type="password" show-password-on="click" /></n-form-item>
        <n-form-item label="新密码"><n-input v-model:value="pwdForm.newPassword" type="password" show-password-on="click" /></n-form-item>
        <n-button type="warning" @click="changePassword" :loading="changingPwd">修改密码</n-button>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { User as UserIcon, Lock, ClipboardList, Heart, MapPin, ShoppingCart } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import http from '@/utils/http'

const message = useMessage()
const userStore = useUserStore()
const saving = ref(false)
const changingPwd = ref(false)
const form = reactive({ nickname: '', phone: '', email: '', gender: 0 })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

onMounted(() => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.phone = userStore.userInfo.phone || ''
    form.email = userStore.userInfo.email || ''
    form.gender = userStore.userInfo.gender || 0
  }
})

const saveProfile = async () => {
  saving.value = true
  try {
    await http.put('/user/profile', form)
    message.success('保存成功')
    await userStore.fetchUserInfo()
  } catch (e: any) { message.error(e.message || '保存失败') }
  finally { saving.value = false }
}

const changePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { message.warning('请填写完整'); return }
  changingPwd.value = true
  try {
    await http.put('/user/password', pwdForm)
    message.success('密码已修改')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch (e: any) { message.error(e.message || '修改失败') }
  finally { changingPwd.value = false }
}
</script>

<style scoped>
.quick-link { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 20px; background: #fff; border-radius: 12px; border: 1px solid #f0f0f0; cursor: pointer; transition: all .2s; }
.quick-link:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(255,107,157,.12); border-color: #FF6B9D44; }
.quick-link-label { font-size: 13px; color: #666; }
</style>
