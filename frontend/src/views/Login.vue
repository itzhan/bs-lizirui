<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #fef0f5, #fff5f0);">
    <n-card style="width: 400px; border-radius: 16px;">
      <div style="text-align: center; margin-bottom: 24px;">
        <Baby :size="56" color="#FF6B9D" :stroke-width="1.5" />
        <h2 style="margin-top: 8px; color: #FF6B9D;">欢迎回来</h2>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="username" label="用户名">
          <n-input v-model:value="form.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" show-password-on="click" @keyup.enter="handleLogin" />
        </n-form-item>
        <n-button type="primary" block size="large" :loading="loading" @click="handleLogin" style="margin-top: 8px;">登录</n-button>
      </n-form>
      <div style="text-align: center; margin-top: 16px; color: #999;">
        还没有账号？<router-link to="/register" style="color: #FF6B9D;">去注册</router-link>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { Baby } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

const handleLogin = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    message.success('登录成功')
    router.push((route.query.redirect as string) || '/')
  } catch (e: any) { message.error(e.message || '登录失败') }
  finally { loading.value = false }
}
</script>
