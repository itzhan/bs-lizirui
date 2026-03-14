<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #fef0f5, #fff5f0);">
    <n-card style="width: 440px; border-radius: 16px;">
      <div style="text-align: center; margin-bottom: 24px;">
        <Baby :size="56" color="#FF6B9D" :stroke-width="1.5" />
        <h2 style="margin-top: 8px; color: #FF6B9D;">注册新账号</h2>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item path="username" label="用户名"><n-input v-model:value="form.username" placeholder="4-20位字母数字" /></n-form-item>
        <n-form-item path="password" label="密码"><n-input v-model:value="form.password" type="password" placeholder="6位以上" show-password-on="click" /></n-form-item>
        <n-form-item path="nickname" label="昵称"><n-input v-model:value="form.nickname" placeholder="您的昵称" /></n-form-item>
        <n-form-item path="phone" label="手机号"><n-input v-model:value="form.phone" placeholder="11位手机号" /></n-form-item>
        <n-button type="primary" block size="large" :loading="loading" @click="handleRegister" style="margin-top: 8px;">注册</n-button>
      </n-form>
      <div style="text-align: center; margin-top: 16px; color: #999;">
        已有账号？<router-link to="/login" style="color: #FF6B9D;">去登录</router-link>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { Baby } from 'lucide-vue-next'
import http from '@/utils/http'

const router = useRouter()
const message = useMessage()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '', phone: '' })
const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  nickname: { required: true, message: '请输入昵称', trigger: 'blur' }
}

const handleRegister = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    await http.post('/auth/register', form)
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (e: any) { message.error(e.message || '注册失败') }
  finally { loading.value = false }
}
</script>
