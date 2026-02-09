-- 매물 조회 서브쿼리 인덱스
CREATE INDEX idx_property_image_property_id_is_deleted_id
    ON property_images (property_id, is_deleted, id);
