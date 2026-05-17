<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">∑</div>
        <h1>欢迎回来</h1>
        <p>登录您的账户继续使用计算器</p>
      </div>
      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
              required
            />
          </div>
        </div>
        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              autocomplete="current-password"
              required
            />
            <span class="toggle-password" @click="showPassword = !showPassword">
              {{ showPassword ? '🙈' : '👁️' }}
            </span>
          </div>
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <button type="submit" class="btn-primary" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <div class="auth-footer">
        还没有账户？
        <router-link to="/register">立即注册</router-link>
      </div>

      <div class="test-accounts">
        <p class="test-title">测试账号</p>
        <div class="test-list">
          <div class="test-item" @click="fillAccount('user')">
            <span class="test-role role-user">普通用户</span>
            <span>user / 123456</span>
          </div>
          <div class="test-item" @click="fillAccount('vip')">
            <span class="test-role role-vip">VIP</span>
            <span>vip / 123456</span>
          </div>
          <div class="test-item" @click="fillAccount('admin')">
            <span class="test-role role-admin">管理员</span>
            <span>admin / 123456</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const form = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)

function fillAccount(type) {
  form.username = type
  form.password = '123456'
  error.value = ''
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const data = await auth.login({ username: form.username, password: form.password })
    router.push(data.role === 'ROLE_ADMIN' ? '/admin' : '/calculator')
  } catch (e) {
    error.value = e.response?.data?.error || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 2rem);
  width: 100%;
}

.auth-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
  padding: 3rem 2.5rem;
  width: 100%;
  max-width: 420px;
  animation: slideUp 0.5s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-logo {
  font-size: 3rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.auth-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.3rem;
}

.auth-header p {
  font-size: 0.9rem;
  color: #8888a0;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 500;
  color: #b0b0c0;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 0 0.8rem;
  transition: all 0.2s;
}

.input-wrapper:focus-within {
  border-color: rgba(102, 126, 234, 0.5);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-icon {
  font-size: 1rem;
  margin-right: 0.5rem;
}

.input-wrapper input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  padding: 0.75rem 0;
  color: #e0e0e0;
  font-size: 0.95rem;
  font-family: inherit;
}

.input-wrapper input::placeholder {
  color: #555570;
}

.toggle-password {
  cursor: pointer;
  font-size: 1rem;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.toggle-password:hover {
  opacity: 1;
}

.error-msg {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #f87171;
  padding: 0.6rem 0.8rem;
  border-radius: 8px;
  font-size: 0.85rem;
}

.btn-primary {
  padding: 0.8rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #8888a0;
}

.auth-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.auth-footer a:hover {
  text-decoration: underline;
}

.test-accounts {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.test-title {
  font-size: 0.75rem;
  color: #666680;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 0.8rem;
}

.test-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.test-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.5rem 0.8rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.test-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.test-role {
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  min-width: 56px;
  text-align: center;
}

.role-user { background: rgba(72, 199, 142, 0.15); color: #48c78e; }
.role-vip { background: rgba(246, 173, 85, 0.15); color: #f6ad55; }
.role-admin { background: rgba(237, 137, 54, 0.15); color: #ed8936; }
</style>