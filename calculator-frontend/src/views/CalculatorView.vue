<template>
  <div class="calculator-page">
    <div class="calc-container">
      <div class="calc-display">
        <div class="display-expression">{{ expression || '0' }}</div>
        <div class="display-result">{{ result }}</div>
      </div>

      <div class="calc-body">
        <div class="calc-operations">
          <button
            v-for="op in operations"
            :key="op.type"
            class="op-btn"
            :class="{
              'op-available': op.available,
              'op-disabled': !op.available,
              'op-active': selectedOp === op.type
            }"
            :disabled="!op.available"
            @click="selectOperation(op)"
          >
            <span class="op-symbol">{{ op.symbol }}</span>
            <span class="op-label">{{ op.label }}</span>
          </button>
        </div>

        <div class="calc-inputs" v-if="selectedOp">
          <div class="input-row">
            <div class="input-group" v-if="selectedOp.needsA">
              <label>操作数 A</label>
              <input
                ref="inputA"
                v-model="operandA"
                type="number"
                step="any"
                placeholder="输入数字"
                @keyup.enter="calculate"
              />
            </div>
            <div class="input-group" v-if="selectedOp.needsB">
              <label>操作数 B</label>
              <input
                v-model="operandB"
                type="number"
                step="any"
                placeholder="输入数字"
                @keyup.enter="calculate"
              />
            </div>
          </div>
          <button class="btn-calculate" @click="calculate" :disabled="calculating">
            <span v-if="calculating" class="spinner"></span>
            {{ calculating ? '计算中...' : '= 计算' }}
          </button>
        </div>

        <div v-if="!selectedOp" class="calc-hint">
          <span class="hint-icon">👆</span>
          请选择一个运算类型开始计算
        </div>
      </div>

      <div v-if="lastResult" class="calc-history">
        <div class="history-title">历史记录</div>
        <div
          v-for="(item, idx) in history"
          :key="idx"
          class="history-item"
        >
          <span class="history-expr">{{ item.expression }}</span>
          <span class="history-eq">=</span>
          <span class="history-result">{{ item.result }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const auth = useAuthStore()

const operations = ref([])
const selectedOp = ref(null)
const operandA = ref('')
const operandB = ref('')
const expression = ref('')
const result = ref('')
const calculating = ref(false)
const history = reactive([])
const inputA = ref(null)

const lastResult = computed(() => history.length > 0)

const opConfigs = {
  ADD: { symbol: '+', label: '加法', needsA: true, needsB: true },
  SUBTRACT: { symbol: '−', label: '减法', needsA: true, needsB: true },
  MULTIPLY: { symbol: '×', label: '乘法', needsA: true, needsB: true },
  DIVIDE: { symbol: '÷', label: '除法', needsA: true, needsB: true },
  SQRT: { symbol: '√', label: '平方根', needsA: true, needsB: false },
  POWER: { symbol: '^', label: '幂运算', needsA: true, needsB: true }
}

onMounted(async () => {
  try {
    const res = await api.get('/calculator/operations')
    const available = res.data.availableOperations
    operations.value = Object.entries(opConfigs).map(([type, config]) => ({
      type,
      ...config,
      available: available[type] || false
    }))
  } catch (e) {
    console.error('Failed to load operations', e)
  }
})

function selectOperation(op) {
  selectedOp.value = op
  expression.value = ''
  result.value = ''
  operandA.value = ''
  operandB.value = ''
  nextTick(() => {
    if (inputA.value) inputA.value.focus()
  })
}

async function calculate() {
  if (!selectedOp.value) return

  calculating.value = true
  try {
    const body = { operation: selectedOp.value.type }
    if (selectedOp.value.needsA) body.operandA = parseFloat(operandA.value)
    if (selectedOp.value.needsB) body.operandB = parseFloat(operandB.value)

    const res = await api.post('/calculator/calculate', body)
    expression.value = res.data.expression
    result.value = res.data.result

    history.unshift({
      expression: res.data.expression,
      result: res.data.result
    })
    if (history.length > 10) history.pop()
  } catch (e) {
    result.value = '错误: ' + (e.response?.data?.error || '计算失败')
    expression.value = ''
  } finally {
    calculating.value = false
  }
}
</script>

<style scoped>
.calculator-page {
  display: flex;
  justify-content: center;
  width: 100%;
}

.calc-container {
  width: 100%;
  max-width: 480px;
}

.calc-display {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  min-height: 100px;
}

.display-expression {
  font-size: 1rem;
  color: #8888a0;
  margin-bottom: 0.5rem;
  min-height: 1.5rem;
  word-break: break-all;
}

.display-result {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  min-height: 2.5rem;
  word-break: break-all;
}

.calc-body {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 1.5rem;
}

.calc-operations {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.6rem;
  margin-bottom: 1.2rem;
}

.op-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  padding: 0.8rem 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.02);
  cursor: pointer;
  transition: all 0.2s;
}

.op-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.12);
}

.op-btn.op-active {
  background: rgba(102, 126, 234, 0.12);
  border-color: rgba(102, 126, 234, 0.4);
}

.op-btn.op-disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.op-symbol {
  font-size: 1.3rem;
  font-weight: 600;
}

.op-available .op-symbol {
  color: #667eea;
}

.op-label {
  font-size: 0.75rem;
  color: #8888a0;
}

.calc-inputs {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

.input-row {
  display: flex;
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.input-group {
  flex: 1;
}

.input-group label {
  display: block;
  font-size: 0.8rem;
  color: #8888a0;
  margin-bottom: 0.3rem;
}

.input-group input {
  width: 100%;
  padding: 0.7rem 0.8rem;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  color: #e0e0e0;
  font-size: 1rem;
  font-family: inherit;
  outline: none;
  transition: all 0.2s;
}

.input-group input:focus {
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.08);
}

.input-group input::placeholder {
  color: #555570;
}

.btn-calculate {
  width: 100%;
  padding: 0.8rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-calculate:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-calculate:disabled {
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

.calc-hint {
  text-align: center;
  padding: 2rem;
  color: #666680;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.hint-icon {
  font-size: 2rem;
}

.calc-history {
  margin-top: 1.5rem;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 1.2rem;
}

.history-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: #666680;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 0.8rem;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  font-size: 0.9rem;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}

.history-item:last-child {
  border-bottom: none;
}

.history-expr {
  color: #8888a0;
  flex: 1;
}

.history-eq {
  color: #667eea;
  font-weight: 600;
}

.history-result {
  color: #a78bfa;
  font-weight: 600;
  font-size: 1rem;
}
</style>