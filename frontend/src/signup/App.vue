<template>
  <div class="auth-container">
    <div class="auth-card">
      <h1 class="auth-title">회원가입</h1>
      <p class="auth-subtitle">부동부동에 가입하고 시작하세요</p>

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
          <label class="form-label" for="name">이름</label>
          <input
            id="name"
            v-model="form.name"
            type="text"
            class="form-input"
            :class="{ error: errors.name }"
            placeholder="이름을 입력하세요"
            @blur="validate('name')"
          />
          <p v-if="errors.name" class="error-message">{{ errors.name }}</p>
        </div>

        <div class="form-group">
          <label class="form-label" for="password">비밀번호</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="form-input"
            :class="{ error: errors.password }"
            placeholder="대/소문자, 숫자, 특수문자 포함 8~20자"
            @blur="validate('password')"
          />
          <p v-if="errors.password" class="error-message">{{ errors.password }}</p>
        </div>

        <!-- 전화번호 + 인증 -->
        <div class="form-group">
          <label class="form-label" for="phone">전화번호</label>
          <div class="input-row">
            <input
              id="phone"
              v-model="form.phone"
              type="tel"
              class="form-input"
              :class="{ error: errors.phone }"
              placeholder="01012345678"
              :disabled="phoneVerified"
              @blur="validate('phone')"
            />
            <button
              type="button"
              class="btn-secondary"
              :disabled="sendingCode || phoneVerified || !form.phone"
              @click="handleSendCode"
            >
              {{ phoneVerified ? '인증완료' : sendingCode ? '전송중...' : codeSent ? '재전송' : '인증요청' }}
            </button>
          </div>
          <p v-if="errors.phone" class="error-message">{{ errors.phone }}</p>
        </div>

        <div v-if="codeSent && !phoneVerified" class="form-group">
          <label class="form-label" for="verifyCode">인증번호</label>
          <div class="input-row">
            <input
              id="verifyCode"
              v-model="verifyCode"
              type="text"
              class="form-input"
              :class="{ error: errors.verifyCode }"
              placeholder="인증번호 입력"
              maxlength="6"
            />
            <button
              type="button"
              class="btn-secondary"
              :disabled="verifying || !verifyCode"
              @click="handleVerifyCode"
            >
              {{ verifying ? '확인중...' : '확인' }}
            </button>
          </div>
          <p v-if="errors.verifyCode" class="error-message">{{ errors.verifyCode }}</p>
          <p v-if="countdown > 0" class="hint-message">남은 시간: {{ formatCountdown }}</p>
        </div>

        <div class="form-group">
          <label class="form-label" for="address">주소</label>
          <input
            id="address"
            v-model="form.address"
            type="text"
            class="form-input"
            :class="{ error: errors.address }"
            placeholder="주소를 입력하세요"
            @blur="validate('address')"
          />
          <p v-if="errors.address" class="error-message">{{ errors.address }}</p>
        </div>

        <div class="form-group">
          <label class="form-label" for="role">역할</label>
          <select
            id="role"
            v-model="form.role"
            class="form-select"
            :class="{ error: errors.role }"
            @blur="validate('role')"
          >
            <option value="" disabled>역할을 선택하세요</option>
            <option value="GENERAL">일반 회원</option>
            <option value="SELLER">판매자</option>
          </select>
          <p v-if="errors.role" class="error-message">{{ errors.role }}</p>
        </div>

        <button type="submit" class="btn-primary" :disabled="loading || !phoneVerified">
          {{ loading ? '가입 중...' : '회원가입' }}
        </button>
      </form>

      <p class="auth-link">
        이미 계정이 있으신가요? <a href="/signin">로그인</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onUnmounted } from 'vue'
import { authSignUp, smsSend, smsVerify } from '../shared/api.js'
import {
  validateEmail,
  validatePassword,
  validateName,
  validatePhone,
  validateAddress,
  validateRole,
} from '../shared/validators.js'

const form = reactive({
  email: '',
  name: '',
  password: '',
  phone: '',
  address: '',
  role: '',
})

const errors = reactive({
  email: '',
  name: '',
  password: '',
  phone: '',
  address: '',
  role: '',
  verifyCode: '',
})

const loading = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

// SMS 인증 상태
const verifyCode = ref('')
const codeSent = ref(false)
const sendingCode = ref(false)
const verifying = ref(false)
const phoneVerified = ref(false)
const countdown = ref(0)
let countdownTimer = null

const formatCountdown = computed(() => {
  const min = Math.floor(countdown.value / 60)
  const sec = countdown.value % 60
  return `${min}:${String(sec).padStart(2, '0')}`
})

function startCountdown(seconds) {
  clearInterval(countdownTimer)
  countdown.value = seconds
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

onUnmounted(() => clearInterval(countdownTimer))

const validators = {
  email: validateEmail,
  name: validateName,
  password: validatePassword,
  phone: validatePhone,
  address: validateAddress,
  role: validateRole,
}

function validate(field) {
  errors[field] = validators[field](form[field])
}

function validateAll() {
  Object.keys(validators).forEach(validate)
  return Object.values(errors).every((e) => !e)
}

async function handleSendCode() {
  errors.verifyCode = ''
  errorMsg.value = ''
  validate('phone')
  if (errors.phone) return

  sendingCode.value = true
  try {
    await smsSend(form.phone)
    codeSent.value = true
    verifyCode.value = ''
    startCountdown(180)
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    sendingCode.value = false
  }
}

async function handleVerifyCode() {
  errors.verifyCode = ''
  errorMsg.value = ''
  if (!verifyCode.value) {
    errors.verifyCode = '인증번호를 입력해주세요.'
    return
  }

  verifying.value = true
  try {
    await smsVerify(form.phone, verifyCode.value)
    phoneVerified.value = true
    clearInterval(countdownTimer)
    countdown.value = 0
  } catch (e) {
    errors.verifyCode = e.message
  } finally {
    verifying.value = false
  }
}

async function handleSubmit() {
  successMsg.value = ''
  errorMsg.value = ''
  if (!validateAll()) return
  if (!phoneVerified.value) {
    errorMsg.value = '전화번호 인증을 완료해주세요.'
    return
  }

  loading.value = true
  try {
    const res = await authSignUp({
      email: form.email,
      name: form.name,
      password: form.password,
      phone: form.phone,
      address: form.address,
      role: form.role,
    })
    const { accessToken, refreshToken } = res.data
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    successMsg.value = '회원가입에 성공했습니다.'
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
