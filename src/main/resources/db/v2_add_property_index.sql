USE budongbudong;
-- 목록 조회 전용
CREATE INDEX idx_property_list
    ON properties (is_deleted, created_at DESC);

-- 매물 조회 복합 인덱스
CREATE INDEX idx_property_type_list
    ON properties (is_deleted, type, created_at DESC);

ANALYZE TABLE properties;