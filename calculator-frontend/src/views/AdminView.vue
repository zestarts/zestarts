<template>
  <div class="admin-page">
    <div class="admin-grid">
      <div class="admin-card users-panel">
        <div class="card-header">
          <h2>👥 用户管理</h2>
          <button class="btn-refresh" @click="store.loadUsers" :disabled="loadingUsers">
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
              <div class="user-avatar" :class="avatarClass(user.role)">{{ user.username.charAt(0).toUpperCase() }}</div>
              <div class="user-detail">
                <div class="user-name-line">
                  <span class="user-name">{{ user.username }}</span>
                  <span class="user-role-tag" :class="roleTagClass(user.role)">{{ roleLabel(user.role) }}</span>
                </div>
                <div class="user-meta">
                  <span v-if="user.lastActiveAt" class="last-active">
                    🟢 {{ formatRelative(user.lastActiveAt) }}
                  </span>
                  <span v-else class="last-active never">⚫ 从未活动</span>
                  <span class="perm-count">{{ user.permissions.length }} 项权限</span>
                </div>
              </div>
            </div>
            <div class="user-controls">
              <select
                :value="user.role"
                @change="changeRole(user, $event.target.value)"
                class="role-select"
              >
                <option value="ROLE_USER">普通用户</option>
                <option value="ROLE_VIP">VIP</option>
                <option value="ROLE_ADMIN">管理员</option>
              </select>
              <button class="ctrl-btn btn-perm" @click="openPermissionEditor(user)">
                ⚡ 赋权
              </button>
              <button
                class="ctrl-btn"
                :class="user.enabled ? 'btn-warn' : 'btn-success'"
                @click="toggleUser(user)"
              >
                {{ user.enabled ? '禁用' : '启用' }}
              </button>
              <button
                class="ctrl-btn btn-danger"
                @click="deleteUser(user)"
                :disabled="user.username === 'admin'"
              >
                🗑
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
            <h3>⚡ 赋予权限 — {{ permissionEditor.user.username }}</h3>
            <button class="modal-close" @click="closePermissionEditor">✕</button>
          </div>
          <div class="modal-body">
            <p class="modal-desc">
              为 <strong>{{ permissionEditor.user.username }}</strong>（{{ roleLabel(permissionEditor.user.role) }}）选择可使用的运算：
            </p>
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
                <span class="perm-icon">{{ opIcons[op] }}</span>
                <span class="perm-label">{{ opLabels[op] }}</span>
              </label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closePermissionEditor">取消</button>
            <button class="btn-save" @click="savePermissions" :disabled="savingPerms">
              {{ savingPerms ? '保存中...' : '保存权限' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { reactive, onMounted, onUnmounted } from 'vue'
import { useActivityStore } from '../stores/activity'

const store = useActivityStore()

const { users, activityLogs, wsConnected, loadingUsers } = store
const savingPerms = reactive({ value: false })

const allOperations = ['ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE', 'SQRT', 'POWER']

const opLabels = {
  ADD: '加法', SUBTRACT: '减法', MULTIPLY: '乘法',
  DIVIDE: '除法', SQRT: '平方根', POWER: '幂运算'
}

const opIcons = {
  ADD: '+', SUBTRACT: '−', MULTIPLY: '×',
  DIVIDE: '÷', SQRT: '√', POWER: '^'
}

const permissionEditor = reactive({
  open: false,
  user: null,
  selected: new Set()
})

onMounted(() => {
  store.loadUsers()
  store.loadRecentActivity()
  store.connectWebSocket()
})

onUnmounted(() => {
  store.disconnectWebSocket()
})

async function changeRole(user, newRole) {
  try {
    await store.changeRole(user.username, newRole)
  } catch (e) {
    alert('修改角色失败: ' + (e.response?.data?.error || '未知错误'))
  }
}

async function toggleUser(user) {
  try {
    await store.toggleUser(user.username)
  } catch (e) {
    alert('操作失败: ' + (e.response?.data?.error || '未知错误'))
  }
}

async function deleteUser(user) {
  if (!confirm(`确定要删除用户 "${user.username}" 吗？`)) return
  try {
    await store.deleteUser(user.username)
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
    await store.savePermissions(permissionEditor.user.username, [...permissionEditor.selected])
    closePermissionEditor()
  } catch (e) {
    alert('保存权限失败: ' + (e.response?.data?.error || '未知错误'))
  } finally {
    savingPerms.value = false
  }
}

function roleLabel(role) {
  const map = { ROLE_USER: '普通用户', ROLE_VIP: 'VIP', ROLE_ADMIN: '管理员' }
  return map[role] || role
}

function roleTagClass(role) {
  return 'tag-' + (role === 'ROLE_ADMIN' ? 'admin' : role === 'ROLE_VIP' ? 'vip' : 'user')
}

function avatarClass(role) {
  return 'avatar-' + (role === 'ROLE_ADMIN' ? 'admin' : role === 'ROLE_VIP' ? 'vip' : 'user')
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

function formatRelative(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚活跃'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  return Math.floor(diff / 86400) + ' 天前'
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
  gap: 0.4rem;
  max-height: 450px;
  overflow-y: auto;
}

.user-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0.6rem 0.7rem;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 10px;
  transition: all 0.2s;
}

.user-row:hover {
  background: rgba(255, 255, 255, 0.04);
}

.user-row.user-disabled {
  opacity: 0.45;
}

.user-main {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  min-width: 0;
  flex: 1;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.avatar-user { background: linear-gradient(135deg, #48c78e, #2ecc71); }
.avatar-vip { background: linear-gradient(135deg, #f6ad55, #ed8936); }
.avatar-admin { background: linear-gradient(135deg, #667eea, #764ba2); }

.user-detail {
  min-width: 0;
}

.user-name-line {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.user-name {
  font-weight: 600;
  font-size: 0.9rem;
}

.user-role-tag {
  font-size: 0.6rem;
  font-weight: 700;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  text-transform: uppercase;
}

.tag-user { background: rgba(72, 199, 142, 0.15); color: #48c78e; }
.tag-vip { background: rgba(246, 173, 85, 0.15); color: #f6ad55; }
.tag-admin { background: rgba(102, 126, 234, 0.15); color: #667eea; }

.user-meta {
  display: flex;
  gap: 0.8rem;
  margin-top: 0.15rem;
  font-size: 0.7rem;
  color: #777790;
}

.last-active {
  color: #48c78e;
}

.last-active.never {
  color: #555570;
}

.perm-count {
  color: #8899dd;
}

.user-controls {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  flex-shrink: 0;
}

.role-select {
  padding: 0.3rem 0.5rem;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  color: #e0e0e0;
  font-size: 0.75rem;
  cursor: pointer;
  outline: none;
}

.role-select:focus {
  border-color: rgba(102, 126, 234, 0.4);
}

.ctrl-btn {
  padding: 0.3rem 0.55rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  background: transparent;
  color: #a0a0b8;
  font-size: 0.7rem;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.ctrl-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.05);
}

.ctrl-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.btn-perm {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.25);
  color: #667eea;
  font-weight: 600;
}

.btn-perm:hover {
  background: rgba(102, 126, 234, 0.18) !important;
  border-color: rgba(102, 126, 234, 0.4) !important;
}

.btn-warn:hover { color: #f6ad55; border-color: rgba(246, 173, 85, 0.3); }
.btn-success:hover { color: #48c78e; border-color: rgba(72, 199, 142, 0.3); }
.btn-danger:hover { color: #ef4444; border-color: rgba(239, 68, 68, 0.3); }

.activity-list {
  max-height: 450px;
  overflow-y: auto;
}

.activity-row {
  display: grid;
  grid-template-columns: 65px 70px 55px 1fr 65px;
  gap: 0.5rem;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  font-size: 0.8rem;
}

.activity-time {
  color: #555570;
  font-size: 0.72rem;
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
  max-width: 440px;
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
  line-height: 1.5;
}

.modal-desc strong {
  color: #e0e0e0;
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
  gap: 0.4rem;
  padding: 0.7rem 0.4rem;
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

.perm-icon {
  font-size: 1rem;
  font-weight: 700;
  color: #667eea;
  width: 20px;
  text-align: center;
}

.perm-checkbox.checked .perm-icon {
  color: #a78bfa;
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