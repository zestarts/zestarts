import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ROLE_ADMIN')

  function setAuth(data) {
    token.value = data.token
    username.value = data.username
    role.value = data.role
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
  }

  function clearAuth() {
    token.value = ''
    username.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }

  async function login(credentials) {
    const res = await api.post('/auth/login', credentials)
    setAuth(res.data)
    return res.data
  }

  async function register(data) {
    const res = await api.post('/auth/register', data)
    setAuth(res.data)
    return res.data
  }

  function logout() {
    clearAuth()
  }

  return { token, username, role, isLoggedIn, isAdmin, login, register, logout, setAuth, clearAuth }
})