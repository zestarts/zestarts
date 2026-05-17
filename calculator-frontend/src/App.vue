<template>
  <div class="app-container">
    <header class="app-header" v-if="auth.isLoggedIn">
      <div class="header-left">
        <div class="logo">
          <span class="logo-icon">∑</span>
          <span class="logo-text">MultiCalc</span>
        </div>
      </div>
      <nav class="header-nav">
        <router-link to="/calculator" class="nav-link" active-class="active">
          <span class="nav-icon">🔢</span>
          计算器
        </router-link>
        <router-link v-if="auth.isAdmin" to="/admin" class="nav-link" active-class="active">
          <span class="nav-icon">⚙️</span>
          管理面板
        </router-link>
      </nav>
      <div class="header-right">
        <div class="user-info">
          <span class="role-badge" :class="roleClass">{{ roleLabel }}</span>
          <span class="username">{{ auth.username }}</span>
        </div>
        <button class="btn-logout" @click="handleLogout">退出</button>
      </div>
    </header>
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()

const roleClass = computed(() => {
  const map = { ROLE_USER: 'role-user', ROLE_VIP: 'role-vip', ROLE_ADMIN: 'role-admin' }
  return map[auth.role] || ''
})

const roleLabel = computed(() => {
  const map = { ROLE_USER: '普通用户', ROLE_VIP: 'VIP', ROLE_ADMIN: '管理员' }
  return map[auth.role] || ''
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  color: #e0e0e0;
  min-height: 100vh;
  overflow-x: hidden;
}

#app {
  min-height: 100vh;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  height: 64px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.logo-icon {
  font-size: 1.6rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.logo-text {
  font-size: 1.2rem;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.header-nav {
  display: flex;
  gap: 0.25rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  text-decoration: none;
  color: #a0a0b8;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #e0e0e0;
}

.nav-link.active {
  background: rgba(102, 126, 234, 0.15);
  color: #667eea;
}

.nav-icon {
  font-size: 1rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.role-badge {
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-user {
  background: rgba(72, 199, 142, 0.15);
  color: #48c78e;
}

.role-vip {
  background: rgba(246, 173, 85, 0.15);
  color: #f6ad55;
}

.role-admin {
  background: rgba(237, 137, 54, 0.15);
  color: #ed8936;
}

.username {
  font-weight: 500;
  font-size: 0.9rem;
}

.btn-logout {
  padding: 0.4rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background: transparent;
  color: #a0a0b8;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.app-main {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>