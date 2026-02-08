USE budongbudong;
-- 목록 조회 전용
CREATE INDEX idx_property_list
    ON properties (is_deleted, created_at DESC);

-- 타입 필터 + 목록
CREATE INDEX idx_property_type_list
    ON properties(type, is_deleted, created_at DESC);

-- 가격 범위
CREATE INDEX idx_property_price
    ON properties(price);

-- 매물 조회 복합 인덱스
CREATE INDEX idx_property_created_at_is_deleted
    ON properties (created_at DESC, is_deleted);

ANALYZE TABLE properties;