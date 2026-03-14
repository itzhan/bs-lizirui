<template>
  <div style="max-width: 700px; margin: 0 auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <div style="display: flex; align-items: center; gap: 8px;">
        <MapPin :size="24" color="#FF6B9D" />
        <h2>收货地址</h2>
      </div>
      <n-button type="primary" @click="openDialog('add')">
        <template #icon><Plus :size="16" /></template>
        新增地址
      </n-button>
    </div>
    <n-spin :show="loading">
      <n-card v-for="a in addresses" :key="a.id" style="margin-bottom: 12px; border-radius: 12px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-weight: 500;">{{ a.receiver }} <span style="color: #999; margin-left: 8px;">{{ a.phone }}</span>
              <n-tag v-if="a.isDefault" type="success" size="small" style="margin-left: 8px;">默认</n-tag>
            </div>
            <div style="font-size: 13px; color: #666; margin-top: 4px;">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
          </div>
          <n-space>
            <n-button text type="primary" @click="openDialog('edit', a)">
              <template #icon><Pencil :size="14" /></template>
              编辑
            </n-button>
            <n-button text type="info" @click="setDefault(a.id)" v-if="!a.isDefault">设为默认</n-button>
            <n-popconfirm @positive-click="deleteAddr(a.id)">
              <template #trigger>
                <n-button text type="error">
                  <template #icon><Trash2 :size="14" /></template>
                  删除
                </n-button>
              </template>
              确认删除？
            </n-popconfirm>
          </n-space>
        </div>
      </n-card>
      <n-empty v-if="!addresses.length" description="暂无收货地址" style="margin-top: 80px;">
        <template #icon><MapPin :size="48" color="#ddd" /></template>
      </n-empty>
    </n-spin>

    <n-modal v-model:show="dialogVisible" preset="card" :title="dialogTitle" style="width: 480px;">
      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="收货人"><n-input v-model:value="form.receiver" /></n-form-item>
        <n-form-item label="手机号"><n-input v-model:value="form.phone" /></n-form-item>
        <n-form-item label="省"><n-input v-model:value="form.province" /></n-form-item>
        <n-form-item label="市"><n-input v-model:value="form.city" /></n-form-item>
        <n-form-item label="区"><n-input v-model:value="form.district" /></n-form-item>
        <n-form-item label="详细地址"><n-input v-model:value="form.detail" type="textarea" /></n-form-item>
        <n-form-item label="默认地址"><n-switch v-model:value="form.isDefault" :checked-value="1" :unchecked-value="0" /></n-form-item>
      </n-form>
      <template #footer><n-button type="primary" @click="submitForm" :loading="submitting">保存</n-button></template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { MapPin, Plus, Pencil, Trash2 } from 'lucide-vue-next'
import http from '@/utils/http'

const message = useMessage()
const loading = ref(false)
const addresses = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增地址')
const submitting = ref(false)
const form = reactive<any>({ receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })

const fetchAddresses = async () => {
  loading.value = true
  try { const r: any = await http.get('/addresses'); addresses.value = r.data || [] }
  finally { loading.value = false }
}

const openDialog = (type: string, row?: any) => {
  dialogTitle.value = type === 'add' ? '新增地址' : '编辑地址'
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: undefined, receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })
  dialogVisible.value = true
}

const submitForm = async () => {
  submitting.value = true
  try {
    if (form.id) await http.put(`/addresses/${form.id}`, form)
    else await http.post('/addresses', form)
    message.success('保存成功')
    dialogVisible.value = false
    fetchAddresses()
  } catch (e: any) { message.error(e.message || '保存失败') }
  finally { submitting.value = false }
}

const setDefault = async (id: number) => { await http.put(`/addresses/${id}/default`); message.success('已设为默认'); fetchAddresses() }
const deleteAddr = async (id: number) => { await http.delete(`/addresses/${id}`); message.success('已删除'); fetchAddresses() }

onMounted(fetchAddresses)
</script>
