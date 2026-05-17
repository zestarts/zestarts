import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api'

export const useActivityStore = defineStore('activity', () => {
  const users = ref([])
  const activityLogs = reactive([])
  const wsConnected = ref(false)
  const loadingUsers = ref(false)
  const loadingActivity = ref(false)

  let stompClient = null

  const onlineUsers = computed(() =>
    users.value.filter(u => u.lastActiveAt).length
  )

  const recentActivity = computed(() => activityLogs.slice(0, 20))

  function connectWebSocket() {
    if (stompClient?.active) return

    const socket = new SockJS('/ws')
    stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        wsConnected.value = true
        stompClient.subscribe('/topic/activity', (message) => {
          const data = JSON.parse(message.body)
          activityLogs.unshift({ ...data })
          if (activityLogs.length > 50) activityLogs.pop()

          const idx = users.value.findIndex(u => u.username === data.username)
          if (idx >= 0) {
            users.value[idx] = {
              ...users.value[idx],
              lastActiveAt: data.timestamp
            }
          }
        })
      },
      onDisconnect: () => {
        wsConnected.value = false
      },
      reconnectDelay: 5000
    })
    stompClient.activate()
  }

  function disconnectWebSocket() {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
      wsConnected.value = false
    }
  }

  async function loadUsers() {
    loadingUsers.value = true
    try {
      const res = await api.get('/admin/users')
      users.value = res.data
      return res.data
    } catch (e) {
      console.error('Failed to load users', e)
      return []
    } finally {
      loadingUsers.value = false
    }
  }

  async function loadRecentActivity() {
    loadingActivity.value = true
    try {
      const res = await api.get('/admin/activity/recent')
      activityLogs.splice(0, activityLogs.length, ...res.data)
      return res.data
    } catch (e) {
      console.error('Failed to load activity', e)
      return []
    } finally {
      loadingActivity.value = false
    }
  }

  async function changeRole(username, newRole) {
    const res = await api.put(`/admin/users/${username}/role`, { role: newRole })
    const idx = users.value.findIndex(u => u.username === username)
    if (idx >= 0) users.value[idx] = res.data
    return res.data
  }

  async function toggleUser(username) {
    await api.put(`/admin/users/${username}/toggle`)
    const idx = users.value.findIndex(u => u.username === username)
    if (idx >= 0) {
      users.value[idx] = { ...users.value[idx], enabled: !users.value[idx].enabled }
    }
  }

  async function deleteUser(username) {
    await api.delete(`/admin/users/${username}`)
    users.value = users.value.filter(u => u.username !== username)
  }

  async function savePermissions(targetUsername, permissions) {
    const res = await api.put('/admin/users/permissions', { targetUsername, permissions })
    const idx = users.value.findIndex(u => u.username === targetUsername)
    if (idx >= 0) users.value[idx] = res.data
    return res.data
  }

  return {
    users,
    activityLogs,
    wsConnected,
    loadingUsers,
    loadingActivity,
    onlineUsers,
    recentActivity,
    connectWebSocket,
    disconnectWebSocket,
    loadUsers,
    loadRecentActivity,
    changeRole,
    toggleUser,
    deleteUser,
    savePermissions
  }
})