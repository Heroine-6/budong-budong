const BASE_URL = '/api/v1/auth'

async function request(endpoint, body) {
  const res = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  if (!res.ok || !data.success) {
    throw new Error(data.message || '요청에 실패했습니다.')
  }
  return data
}

export function authSignIn({ email, password }) {
  return request('/signin', { email, password })
}

export function authSignUp({ email, name, password, phone, address, role }) {
  return request('/signup', { email, name, password, phone, address, role })
}

export function smsSend(toNumber) {
  return request('/send', { toNumber })
}

export function smsVerify(toNumber, code) {
  return request('/verify', { toNumber, code })
}
