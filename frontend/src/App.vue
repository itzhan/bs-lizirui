<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <router-view />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import type { GlobalThemeOverrides } from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#FF6B9D',
    primaryColorHover: '#FF85B1',
    primaryColorPressed: '#E55588',
    borderRadius: '8px',
    fontFamily: '"Noto Sans SC", -apple-system, BlinkMacSystemFont, sans-serif'
  }
}
</script>
