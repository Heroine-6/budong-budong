<template>
  <div class="page">
    <!-- 헤더 -->
    <header class="header">
      <div class="header-inner">
        <a href="/" class="logo">부동부동</a>
        <nav class="header-nav">
          <a href="/search" class="btn-text">매물 검색</a>
          <template v-if="isLoggedIn">
            <span class="user-greeting">{{ userName }}님</span>
            <button class="btn-text" @click="logout">로그아웃</button>
          </template>
          <template v-else>
            <a href="/signin" class="btn-text">로그인</a>
            <a href="/signup" class="btn-header-primary">회원가입</a>
          </template>
        </nav>
      </div>
    </header>

    <!-- 히어로 -->
    <section class="hero">
      <div class="hero-inner">
        <h1 class="hero-title">믿을 수 있는 부동산 경매</h1>
        <p class="hero-desc">원하는 매물을 검색하고, 경매에 참여하세요</p>
        <div class="search-bar">
          <input
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="지역명, 단지명으로 검색"
            @keyup.enter="doSearch"
          />
          <button class="search-btn" @click="doSearch">검색</button>
        </div>
      </div>
    </section>

    <!-- 필터 -->
    <section class="filter-section">
      <div class="filter-inner">
        <div class="filter-group">
          <span class="filter-label">매물 유형</span>
          <div class="filter-chips">
            <button
              v-for="t in propertyTypes"
              :key="t.value"
              class="chip"
              :class="{ active: selectedType === t.value }"
              @click="selectType(t.value)"
            >
              {{ t.label }}
            </button>
          </div>
        </div>
        <div class="filter-group">
          <span class="filter-label">경매 상태</span>
          <div class="filter-chips">
            <button
              v-for="s in auctionStatuses"
              :key="s.value"
              class="chip"
              :class="{ active: selectedStatus === s.value }"
              @click="selectStatus(s.value)"
            >
              {{ s.label }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 매물 목록 -->
    <section class="listing-section">
      <div class="listing-inner">
        <div v-if="loading" class="loading-state">매물을 불러오는 중...</div>
        <div v-else-if="properties.length === 0" class="empty-state">
          <p class="empty-icon">&#x1F3E0;</p>
          <p>조건에 맞는 매물이 없습니다</p>
        </div>
        <div v-else class="property-grid">
          <a
            v-for="item in properties"
            :key="item.id"
            :href="'/api/v1/properties/' + item.id"
            class="property-card"
          >
            <div class="card-thumb">
              <img
                v-if="item.thumbnailImage"
                :src="item.thumbnailImage"
                :alt="item.name"
                class="thumb-img"
              />
              <div v-else class="thumb-placeholder">&#x1F3E2;</div>
              <span v-if="item.auction" class="badge" :class="badgeClass(item.auction.status)">
                {{ statusLabel(item.auction.status) }}
              </span>
            </div>
            <div class="card-body">
              <div class="card-type">{{ typeLabel(item.type) }}</div>
              <h3 class="card-name">{{ item.name }}</h3>
              <p class="card-address">{{ item.address }}</p>
              <div class="card-meta">
                <span v-if="item.supplyArea">{{ item.supplyArea }}m&sup2;</span>
                <span v-if="item.privateArea" class="meta-sep">전용 {{ item.privateArea }}m&sup2;</span>
              </div>
              <div v-if="item.auction" class="card-price">
                {{ formatPrice(item.auction.startPrice) }}
              </div>
            </div>
          </a>
        </div>

        <!-- 더보기 -->
        <div v-if="hasNext && !loading" class="load-more">
          <button class="btn-outline" :disabled="loadingMore" @click="loadMore">
            {{ loadingMore ? '불러오는 중...' : '더보기' }}
          </button>
        </div>
      </div>
    </section>

    <!-- 푸터 -->
    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; 2025 부동부동. 부동산 경매 플랫폼</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const propertyTypes = [
  { label: '전체', value: '' },
  { label: '아파트', value: 'APARTMENT' },
  { label: '빌라', value: 'VILLA' },
  { label: '오피스텔', value: 'OFFICETEL' },
]

const auctionStatuses = [
  { label: '전체', value: '' },
  { label: '경매 예정', value: 'SCHEDULED' },
  { label: '진행 중', value: 'OPEN' },
  { label: '종료', value: 'CLOSED' },
]

const searchKeyword = ref('')
const selectedType = ref('')
const selectedStatus = ref('')
const properties = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasNext = ref(false)
const page = ref(0)
const isLoggedIn = ref(false)
const userName = ref('')

onMounted(() => {
  checkAuth()
  fetchProperties()
})

function decodeJwtPayload(token) {
  const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
  const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0))
  return JSON.parse(new TextDecoder().decode(bytes))
}

function checkAuth() {
  const token = localStorage.getItem('accessToken')
  if (!token) return
  try {
    const payload = decodeJwtPayload(token)
    isLoggedIn.value = true
    userName.value = payload.username || payload.userEmail || ''
  } catch {
    isLoggedIn.value = false
  }
}

function logout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  isLoggedIn.value = false
  userName.value = ''
}

function buildSearchParams() {
  const params = new URLSearchParams()
  if (searchKeyword.value) params.set('address', searchKeyword.value)
  if (selectedType.value) params.set('type', selectedType.value)
  if (selectedStatus.value) params.set('status', selectedStatus.value)
  params.set('page', String(page.value))
  params.set('size', '12')
  return params
}

async function fetchProperties() {
  loading.value = true
  page.value = 0
  try {
    const params = buildSearchParams()
    const useSearch = searchKeyword.value || selectedType.value || selectedStatus.value
    const endpoint = useSearch ? '/api/v1/properties/search' : '/api/v1/properties'
    const res = await fetch(`${endpoint}?${params}`)
    const json = await res.json()
    if (json.success && json.data) {
      properties.value = json.data.content || []
      hasNext.value = json.data.hasNext || false
    }
  } catch {
    properties.value = []
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  page.value++
  try {
    const params = buildSearchParams()
    const useSearch = searchKeyword.value || selectedType.value || selectedStatus.value
    const endpoint = useSearch ? '/api/v1/properties/search' : '/api/v1/properties'
    const res = await fetch(`${endpoint}?${params}`)
    const json = await res.json()
    if (json.success && json.data) {
      properties.value.push(...(json.data.content || []))
      hasNext.value = json.data.hasNext || false
    }
  } catch {
    /* ignore */
  } finally {
    loadingMore.value = false
  }
}

function doSearch() {
  if (searchKeyword.value) {
    window.location.href = `/search?keyword=${encodeURIComponent(searchKeyword.value)}`
    return
  }
  fetchProperties()
}

function selectType(val) {
  selectedType.value = val
  fetchProperties()
}

function selectStatus(val) {
  selectedStatus.value = val
  fetchProperties()
}

function typeLabel(type) {
  const map = { APARTMENT: '아파트', VILLA: '빌라', OFFICETEL: '오피스텔' }
  return map[type] || type
}

function statusLabel(status) {
  const map = {
    SCHEDULED: '경매 예정',
    OPEN: '진행 중',
    CLOSED: '종료',
    FAILED: '유찰',
    CANCELLED: '취소',
  }
  return map[status] || status
}

function badgeClass(status) {
  return {
    'badge-scheduled': status === 'SCHEDULED',
    'badge-open': status === 'OPEN',
    'badge-closed': status === 'CLOSED' || status === 'FAILED' || status === 'CANCELLED',
  }
}

function formatPrice(price) {
  if (!price) return ''
  const num = Number(price)
  if (num >= 100000000) {
    const eok = Math.floor(num / 100000000)
    const man = Math.floor((num % 100000000) / 10000)
    return man > 0 ? `${eok}억 ${man.toLocaleString()}만원` : `${eok}억원`
  }
  if (num >= 10000) {
    return `${Math.floor(num / 10000).toLocaleString()}만원`
  }
  return `${num.toLocaleString()}원`
}
</script>

<style scoped>
/* ---------- 헤더 ---------- */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid var(--color-border);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 22px;
  font-weight: 800;
  color: var(--color-primary);
  text-decoration: none;
}
.header-nav {
  display: flex;
  align-items: center;
  gap: 12px;
}
.btn-text {
  background: none;
  border: none;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-secondary);
  cursor: pointer;
  text-decoration: none;
  padding: 8px 12px;
}
.btn-text:hover {
  color: var(--color-text);
}
.btn-header-primary {
  padding: 8px 18px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: var(--color-primary);
  border-radius: 8px;
  text-decoration: none;
  transition: background 0.2s;
}
.btn-header-primary:hover {
  background: var(--color-primary-hover);
}
.user-greeting {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

/* ---------- 히어로 ---------- */
.hero {
  background: linear-gradient(135deg, #3182f6 0%, #1b64da 100%);
  padding: 64px 24px;
}
.hero-inner {
  max-width: 640px;
  margin: 0 auto;
  text-align: center;
}
.hero-title {
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 8px;
}
.hero-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 28px;
}
.search-bar {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}
.search-input {
  flex: 1;
  padding: 16px 20px;
  font-size: 16px;
  border: none;
  outline: none;
  color: var(--color-text);
}
.search-input::placeholder {
  color: var(--color-text-secondary);
}
.search-btn {
  padding: 16px 28px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: var(--color-primary);
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}
.search-btn:hover {
  background: var(--color-primary-hover);
}

/* ---------- 필터 ---------- */
.filter-section {
  background: #fff;
  border-bottom: 1px solid var(--color-border);
  padding: 20px 24px;
}
.filter-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  white-space: nowrap;
}
.filter-chips {
  display: flex;
  gap: 6px;
}
.chip {
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: var(--color-input-bg);
  border: 1px solid transparent;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}
.chip:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
}
.chip.active {
  color: #fff;
  background: var(--color-primary);
  border-color: var(--color-primary);
}

/* ---------- 매물 목록 ---------- */
.listing-section {
  padding: 32px 24px 64px;
}
.listing-inner {
  max-width: 1200px;
  margin: 0 auto;
}
.property-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 24px;
}
.property-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
  text-decoration: none;
  color: inherit;
}
.property-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
.card-thumb {
  position: relative;
  width: 100%;
  height: 180px;
  background: var(--color-input-bg);
  overflow: hidden;
}
.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: #e8edf3;
}
.badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  border-radius: 6px;
  color: #fff;
}
.badge-scheduled {
  background: #6b7684;
}
.badge-open {
  background: #2eb67d;
}
.badge-closed {
  background: var(--color-error);
}
.card-body {
  padding: 16px;
}
.card-type {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 4px;
}
.card-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-address {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-meta {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}
.meta-sep::before {
  content: '·';
  margin: 0 4px;
}
.card-price {
  font-size: 18px;
  font-weight: 800;
  color: var(--color-text);
}

/* ---------- 더보기 ---------- */
.load-more {
  text-align: center;
  margin-top: 40px;
}
.btn-outline {
  padding: 12px 40px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-primary);
  background: #fff;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s;
}
.btn-outline:hover:not(:disabled) {
  background: var(--color-primary);
  color: #fff;
}
.btn-outline:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ---------- 상태 표시 ---------- */
.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-secondary);
  font-size: 16px;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* ---------- 푸터 ---------- */
.footer {
  background: #f2f4f6;
  padding: 32px 24px;
  border-top: 1px solid var(--color-border);
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-secondary);
}

/* ---------- 반응형 ---------- */
@media (max-width: 640px) {
  .hero-title {
    font-size: 24px;
  }
  .hero {
    padding: 40px 16px;
  }
  .filter-inner {
    flex-direction: column;
    gap: 12px;
  }
  .property-grid {
    grid-template-columns: 1fr;
  }
}
</style>
