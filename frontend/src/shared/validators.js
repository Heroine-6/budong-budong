export function validateEmail(value) {
  if (!value) return '이메일은 필수입니다.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return '이메일 형식이 올바르지 않습니다.'
  return ''
}

export function validatePassword(value) {
  if (!value) return '비밀번호는 필수입니다.'
  if (value.length < 8 || value.length > 20) return '비밀번호는 8자 이상 20자 이하로 입력해주세요.'
  if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-={}[\]:;"'<>,.?/])/.test(value)) {
    return '비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.'
  }
  return ''
}

export function validateName(value) {
  if (!value) return '이름은 필수입니다.'
  if (value.length > 50) return '이름은 최대 50자까지 입력 가능합니다.'
  return ''
}

export function validatePhone(value) {
  if (!value) return '전화번호는 필수입니다.'
  if (!/^[0-9]+$/.test(value)) return '전화번호는 하이픈(-) 없이 숫자만 입력해주세요.'
  if (value.length > 50) return '전화번호는 최대 50자까지 입력 가능합니다.'
  return ''
}

export function validateAddress(value) {
  if (!value) return '주소는 필수입니다.'
  if (value.length > 100) return '주소는 최대 100자까지 입력 가능합니다.'
  return ''
}

export function validateRole(value) {
  if (!value) return '역할은 필수입니다.'
  return ''
}
