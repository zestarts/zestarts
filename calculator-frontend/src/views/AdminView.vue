<template>
  <div class="admin-page">
    <div class="admin-grid">
      <div class="admin-card users-panel">
        <div class="card-header">
          <h2>👥 用户管理</h2>
          <button class="btn-refresh" @click="loadUsers" :disabled="loadingUsers">
            🔄 刷新
          </button>
        </div>
        <div class="users-list">
          <div
            v-for="user in users"
            :key="user.id"
            class="user-row"
            :class="{ 'user-disabled': !user.enabled }"
          >
            <div class="user-main">
              <div class="user-avatar">{{ user.username.charAt(0).toUpperCase() }}</div>
              <div class="user-detail">
                <div class="user-name">{{ user.username }}</div>
                <div class="user-perms">
                  <span
                    v-for="perm in user.permissions"
                    :key="perm"
                    class="perm-tag"
                  >{{ permLabels[perm] || perm }}</span>
                </div>
              </div>
            </div>
            <div class="user-role">
              <select
                :value="user.role"
                @change="changeRole(user, $event.target.value)"
                class="role-select"
              >
                <option value="ROLE_USER">普通用户</option>
                <option value="ROLE_VIP">VIP</option>
                <option value="ROLE_ADMIN">管理员</option>
              </select>
            </div>
            <div class="user-actions">
              <button
                class="action-btn"
                :class="user.enabled ? 'btn-warn' : 'btn-success'"
                @click="toggleUser(user)"
              >
                {{ user.enabled ? '禁用' : '启用' }}
              </button>
              <button
                class="action-btn btn-perm"
                @click="openPermissionEditor(user)"
              >
                权限
              </button>
              <button
                class="action-btn btn-danger"
                @click="deleteUser(user)"
                :disabled="user.username === 'admin'"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="admin-card monitor-panel">
        <div class="card-header">
          <h2>📡 实时监控</h2>
          <span class="live-dot" :class="{ connected: wsConnected }">
            {{ wsConnected ? '已连接' : '未连接' }}
          </span>
        </div>
        <div class="activity-list">
          <TransitionGroup name="activity-item">
            <div
              v-for="(log, idx) in activityLogs"
              :key="log.id || idx"
              class="activity-row"
            >
              <div class="activity-time">{{ formatTime(log.timestamp) }}</div>
              <div class="activity-user">{{ log.username }}</div>
              <div class="activity-op">
                <span class="op-tag">{{ opLabels[log.operation] || log.operation }}</span>
              </div>
              <div class="activity-expr">{{ log.expression }}</div>
              <div class="activity-result">= {{ log.result }}</div>
            </div>
          </TransitionGroup>
          <div v-if="activityLogs.length === 0" class="empty-state">
            📭 暂无操作记录
          </div>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="permissionEditor.open" class="modal-overlay" @click.self="closePermissionEditor">
        <div class="modal-card">
          <div class="modal-header">
            <h3>编辑权限 - {{ permissionEditor.user.username }}</h3>
            <button class="modal-close" @click="closePermissionEditor">✕</button>
          </div>
          <div class="modal-body">
            <p class="modal-desc">选择该用户可以使用的运算类型：</p>
            <div class="perm-grid">
              <label
                v-for="op in allOperations"
                :key="op"
                class="perm-checkbox"
                :class="{ checked: permissionEditor.selected.has(op) }"
              >
                <input
                  type="checkbox"
                  :value="op"
                  :checked="permissionEditor.selected.has(op)"
                  @change="togglePerm(op)"
                />
                <span class="perm-label">{{ opLabels[op] }}</span>
              </label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closePermissionEditor">取消</button>
            <button class="btn-save" @click="savePermissions" :disabled="savingPerms">
              {{ savingPerms ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api'

const users = ref([])
const loadingUsers = ref(false)
const activityLogs = reactive([])
const wsConnected = ref(false)
const savingPerms = ref(false)

let stompClient = null

const allOperations = ['ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE', 'SQRT', 'POWER']

const permLabels = {
  ADD: '加法', SUBTRACT: '减法', MULTIPLY: '乘法',
  DIVIDE: '除法', SQRT: '平方根', POWER: '幂运算'
}

const opLabels = { ...permLabels }

const permissionEditor = reactive({
  open: false,
  user: null,
  selected: new Set()
})

onMounted(() => {
  loadUsers()
  loadRecentActivity()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) stompClient.deactivate()
})

async function loadUsers() {
  loadingUsers.value = true
  try {
    const res = await api.get('/admin/users')
    users.value = res.data
  } catch (e) {
    console.error('Failed to load users', e)
  } finally {
    loadingUsers.value = false
  }
}

async function loadRecentActivity() {
  try {
    const res = await api.get('/admin/activity/recent')
    activityLogs.splice(0, activityLogs.length, ...res.data)
  } catch (e) {
    console.error('Failed to load activity', e)
  }
}

function connectWebSocket() {
  const socket = new SockJS('/ws')
  stompClient = new Client({
    webSocketFactory: () => socket,
    onConnect: () => {
      wsConnected.value = true
      stompClient.subscribe('/topic/activity', (message) => {
        const data = JSON.parse(message.body)
        activityLogs.unshift({ ...data })
        if (activityLogs.length > 50) activityLogs.pop()
      })
    },
    onDisconnect: () => {
      wsConnected.value = false
    },
    reconnectDelay: 5000
  })
  stompClient.activate()
}

async function changeRole(user, newRole) {
  try {
    const res = await api.put(`/admin/users/${user.username}/role`, { role: newRole })
    Object.assign(user, res.data)
  } catch (e) {
    alert('修改角色失败: ' + (e.response?.data?.error || '未知错误'))
  }
}

async function toggleUser(user) {
  try {
    await api.put(`/admin/users/${user.username}/toggle`)
    user.enabled = !user.enabled
  } catch (e) {
    alert('操作失败: ' + (e.response?.data?.error || '未知错误'))
  }
}

async function deleteUser(user) {
  if (!confirm(`确定要删除用户 "${user.username}" 吗？`)) return
  try {
    await api.delete(`/admin/users/${user.username}`)
    users.value = users.value.filter(u => u.id !== user.id)
  } catch (e) {
    alert('删除失败: ' + (e.response?.data?.error || '未知错误'))
  }
}

function openPermissionEditor(user) {
  permissionEditor.user = user
  permissionEditor.selected = new Set(user.permissions)
  permissionEditor.open = true
}

function closePermissionEditor() {
  permissionEditor.open = false
  permissionEditor.user = null
  permissionEditor.selected = new Set()
}

function togglePerm(op) {
  if (permissionEditor.selected.has(op)) {
    permissionEditor.selected.delete(op)
  } else {
    permissionEditor.selected.add(op)
  }
}

async function savePermissions() {
  savingPerms.value = true
  try {
    const res = await api.put('/admin/users/permissions', {
      targetUsername: permissionEditor.user.username,
      permissions: [...permissionEditor.selected]
    })
    const idx = users.value.findIndex(u => u.id === permissionEditor.user.id)
    if (idx >= 0) users.value[idx] = res.data
    closePermissionEditor()
  } catch (e) {
    alert('保存权限失败: ' + (e.response?.data?.error || '未知错误'))
  } finally {
    savingPerms.value = false
  }
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}
</script>

<style scoped>
.admin-page {
  width: 100%;
  max-width: 1100px;
}

.admin-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

@media (max-width: 900px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }
}

.admin-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.5rem;
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.card-header h2 {
  font-size: 1.1rem;
  font-weight: 600;
}

.btn-refresh {
  padding: 0.3rem 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  background: transparent;
  color: #8888a0;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-refresh:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #e0e0e0;
}

.live-dot {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.8rem;
  color: #666680;
}

.live-dot::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #666680;
}

.live-dot.connected {
  color: #48c78e;
}

.live-dot.connected::before {
  background: #48c78e;
  box-shadow: 0 0 8px rgba(72, 199, 142, 0.5);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 450px;
  overflow-y: auto;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.7rem;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 10px;
  transition: all 0.2s;
}

.user-row:hover {
  background: rgba(255, 255, 255, 0.04);
}

.user-row.user-disabled {
  opacity: 0.5;
}

.user-main {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex: 1;
  min-width: 0;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.user-name {
  font-weight: 500;
  font-size: 0.9rem;
}

.user-perms {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
  margin-top: 0.2rem;
}

.perm-tag {
  font-size: 0.65rem;
  padding: 0.1rem 0.4rem;
  background: rgba(102, 126, 234, 0.12);
  color: #8899dd;
  border-radius: 4px;
}

.user-role {
  flex-shrink: 0;
}

.role-select {
  padding: 0.3rem 0.5rem;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.8rem;
  cursor: pointer;
  outline: none;
}

.role-select:focus {
  border-color: rgba(102, 126, 234, 0.4);
}

.user-actions {
  display: flex;
  gap: 0.3rem;
  flex-shrink: 0;
}

.action-btn {
  padding: 0.25rem 0.55rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  background: transparent;
  color: #a0a0b8;
  font-size: 0.7rem;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.05);
}

.action-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.btn-warn:hover { color: #f6ad55; border-color: rgba(246, 173, 85, 0.3); }
.btn-success:hover { color: #48c78e; border-color: rgba(72, 199, 142, 0.3); }
.btn-perm:hover { color: #667eea; border-color: rgba(102, 126, 234, 0.3); }
.btn-danger:hover { color: #ef4444; border-color: rgba(239, 68, 68, 0.3); }

.activity-list {
  max-height: 450px;
  overflow-y: auto;
}

.activity-row {
  display: grid;
  grid-template-columns: 70px 70px 55px 1fr 70px;
  gap: 0.5rem;
  align-items: center;
  padding: 0.55rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  font-size: 0.8rem;
}

.activity-time {
  color: #555570;
  font-size: 0.75rem;
}

.activity-user {
  font-weight: 500;
  color: #b0b0c0;
}

.op-tag {
  font-size: 0.65rem;
  padding: 0.1rem 0.4rem;
  background: rgba(102, 126, 234, 0.12);
  color: #8899dd;
  border-radius: 4px;
}

.activity-expr {
  color: #8888a0;
}

.activity-result {
  color: #a78bfa;
  font-weight: 600;
  text-align: right;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #555570;
}

.activity-item-enter-active {
  transition: all 0.4s ease;
}

.activity-item-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-card {
  background: #1a1a2e;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  width: 90%;
  max-width: 420px;
  animation: scaleIn 0.2s ease;
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.modal-header h3 {
  font-size: 1rem;
  font-weight: 600;
}

.modal-close {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: transparent;
  color: #8888a0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.modal-close:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.modal-body {
  padding: 1.2rem 1.5rem;
}

.modal-desc {
  font-size: 0.85rem;
  color: #8888a0;
  margin-bottom: 1rem;
}

.perm-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
}

.perm-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.6rem;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.perm-checkbox input {
  display: none;
}

.perm-checkbox.checked {
  border-color: rgba(102, 126, 234, 0.4);
  background: rgba(102, 126, 234, 0.08);
}

.perm-checkbox:hover {
  border-color: rgba(255, 255, 255, 0.15);
}

.perm-label {
  font-size: 0.85rem;
  font-weight: 500;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.btn-cancel {
  padding: 0.5rem 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background: transparent;
  color: #a0a0b8;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.05);
}

.btn-save {
  padding: 0.5rem 1.5rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover:not(:disabled) {
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-save:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>