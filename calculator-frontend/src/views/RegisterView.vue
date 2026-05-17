<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">∑</div>
        <h1>创建账户</h1>
        <p>注册后即可使用计算器功能</p>
      </div>
      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input v-model="form.username" type="text" placeholder="请输入用户名" required />
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
              required
            />
            <span class="toggle-password" @click="showPassword = !showPassword">
              {{ showPassword ? '🙈' : '👁️' }}
            </span>
          </div>
        </div>
        <div class="form-group">
          <label>角色</label>
          <div class="role-selector">
            <label
              v-for="role in roles"
              :key="role.value"
              class="role-option"
              :class="{ selected: form.role === role.value }"
            >
              <input type="radio" v-model="form.role" :value="role.value" />
              <span class="role-card">
                <span class="role-icon">{{ role.icon }}</span>
                <span class="role-name">{{ role.label }}</span>
                <span class="role-desc">{{ role.desc }}</span>
              </span>
            </label>
          </div>
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <button type="submit" class="btn-primary" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>
      <div class="auth-footer">
        已有账户？
        <router-link to="/login">立即登录</router-link>
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

const form = reactive({ username: '', password: '', role: 'ROLE_USER' })
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)

const roles = [
  { value: 'ROLE_USER', label: '普通用户', icon: '👤', desc: '仅限加减法运算' },
  { value: 'ROLE_VIP', label: 'VIP 用户', icon: '⭐', desc: '所有计算功能' },
  { value: 'ROLE_ADMIN', label: '管理员', icon: '🛡️', desc: '全部功能 + 用户管理' }
]

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    const data = await auth.register({
      username: form.username,
      password: form.password,
      role: form.role
    })
    router.push(data.role === 'ROLE_ADMIN' ? '/admin' : '/calculator')
  } catch (e) {
    error.value = e.response?.data?.error || '注册失败，请重试'
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
  padding: 2.5rem 2.5rem;
  width: 100%;
  max-width: 460px;
  animation: slideUp 0.5s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-header {
  text-align: center;
  margin-bottom: 1.8rem;
}

.auth-logo {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.3rem;
}

.auth-header h1 {
  font-size: 1.4rem;
  font-weight: 600;
  margin-bottom: 0.3rem;
}

.auth-header p {
  font-size: 0.85rem;
  color: #8888a0;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
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
  padding: 0.7rem 0;
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

.role-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.6rem;
}

.role-option input {
  display: none;
}

.role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  padding: 0.8rem 0.4rem;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.role-option.selected .role-card {
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(102, 126, 234, 0.08);
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.15);
}

.role-option:hover .role-card {
  border-color: rgba(255, 255, 255, 0.15);
}

.role-icon {
  font-size: 1.3rem;
}

.role-name {
  font-size: 0.8rem;
  font-weight: 600;
}

.role-desc {
  font-size: 0.65rem;
  color: #777790;
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
  margin-top: 0.3rem;
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
  margin-top: 1.2rem;
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
</style>