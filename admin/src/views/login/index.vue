<template>
  <div class="login-container flx-center">
    <div class="login-box">
      <SwitchDark class="dark" />
      <div class="login-left">
        <img class="login-left-img" src="@/assets/images/login_left.png" alt="login" />
      </div>
      <div class="login-form">
        <div class="login-logo">
          <span class="logo-icon">🍼</span>
          <div>
            <h2 class="logo-text">母婴乐园</h2>
            <p class="logo-sub">后台管理系统 · Admin Panel</p>
          </div>
        </div>
        <LoginForm />
        <div class="login-footer">
          <p>技术栈：Spring Boot 3.2 + Vue 3 + Element Plus</p>
          <p>© 2024 母婴乐园商城 — 毕业设计作品</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="login">
import LoginForm from "./components/LoginForm.vue";
import SwitchDark from "@/components/SwitchDark/index.vue";
</script>

<style scoped lang="scss">
@import "./index.scss";
.login-logo {
  .logo-icon {
    font-size: 48px;
    margin-right: 16px;
  }
  .logo-sub {
    margin: 4px 0 0;
    font-size: 13px;
    color: #909399;
    letter-spacing: 1px;
  }
}
.login-footer {
  margin-top: 32px;
  text-align: center;
  p {
    margin: 4px 0;
    font-size: 12px;
    color: #c0c4cc;
  }
}
</style>
