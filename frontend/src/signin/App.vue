<template>
  <div class="auth-container">
    <div class="auth-card">
      <h1 class="auth-title">로그인</h1>
      <p class="auth-subtitle">부동부동에 오신 것을 환영합니다</p>

      <div v-if="successMsg" class="alert alert-success">{{ successMsg }}</div>
      <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>

      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label class="form-label" for="email">이메일</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="form-input"
            :class="{ error: errors.email }"
            placeholder="example@email.com"
            @blur="validate('email')"
          />
          <p v-if="errors.email" class="error-message">{{ errors.email }}</p>
        </div>

        <div class="form-group">
          <label class="form-label" for="password">비밀번호</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="form-input"
            :class="{ error: errors.password }"
            placeholder="비밀번호를 입력하세요"
            @blur="validate('password')"
          />
          <p v-if="errors.password" class="error-message">{{ errors.password }}</p>
        </div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <p class="auth-link">
        계정이 없으신가요? <a href="/signup">회원가입</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { authSignIn } from '../shared/api.js'
import { validateEmail, validatePassword } from '../shared/validators.js'

const form = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })
const loading = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

const validators = { email: validateEmail, password: validatePassword }

function validate(field) {
  errors[field] = validators[field](form[field])
}

function validateAll() {
  validate('email')
  validate('password')
  return !errors.email && !errors.password
}

async function handleSubmit() {
  successMsg.value = ''
  errorMsg.value = ''
  if (!validateAll()) return

  loading.value = true
  try {
    const res = await authSignIn({ email: form.email, password: form.password })
    const { accessToken, refreshToken } = res.data
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    window.location.href = '/budongbudong'
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
