
-- =========================
-- Users
-- =========================
INSERT INTO users (email, name, password, phone, address, role, created_at, is_deleted) VALUES
                                                                                            ('user1@test.com', '일반유저1', 'password123', '010-1000-0001', '서울시 테스트구 1번지', 'GENERAL', NOW(), false),
                                                                                            ('user2@test.com', '일반유저2', 'password123', '010-1000-0002', '서울시 테스트구 2번지', 'GENERAL', NOW(), false),
                                                                                            ('user3@test.com', '일반유저3', 'password123', '010-1000-0003', '서울시 테스트구 3번지', 'GENERAL', NOW(), false),
                                                                                            ('user4@test.com', '일반유저4', 'password123', '010-1000-0004', '서울시 테스트구 4번지', 'GENERAL', NOW(), false),
                                                                                            ('user5@test.com', '일반유저5', 'password123', '010-1000-0005', '서울시 테스트구 5번지', 'GENERAL', NOW(), false),
                                                                                            ('user6@test.com', '일반유저6', 'password123', '010-1000-0006', '서울시 테스트구 6번지', 'GENERAL', NOW(), false),

                                                                                            ('seller1@test.com', '판매자1', 'password123', '010-2000-0001', '서울시 판매구 1번지', 'SELLER', NOW(), false),
                                                                                            ('seller2@test.com', '판매자2', 'password123', '010-2000-0002', '서울시 판매구 2번지', 'SELLER', NOW(), false),
                                                                                            ('seller3@test.com', '판매자3', 'password123', '010-2000-0003', '서울시 판매구 3번지', 'SELLER', NOW(), false),

                                                                                            ('admin@test.com', '관리자', 'password123', '010-9000-0001', '서울시 관리자구', 'ADMIN', NOW(), false);

-- =========================
-- Properties (SELLER id: 7,8,9)
-- =========================
INSERT INTO properties
(user_id, name, address, floor, total_floor, room_count, type, built_year, description,
 price, migrate_date, supply_area, private_area, created_at, is_deleted)
VALUES
    (7, '테스트 아파트 1', '서울시 테스트구 A동', 5, 20, 3, 'APARTMENT', '2015', '남향, 역세권', 450000000, '2026-03-01', 84.5, 59.8, NOW(), false),
    (7, '테스트 빌라 1', '서울시 테스트구 B동', 2, 5, 2, 'VILLA', '2012', '조용한 주택가', 280000000, '2026-02-15', 55.2, 42.1, NOW(), false),
    (7, '테스트 오피스텔 1', '서울시 테스트구 C동', 10, 18, 1, 'OFFICETEL', '2020', '풀옵션', 320000000, '2026-01-20', 40.0, 28.0, NOW(), false),

    (8, '테스트 아파트 2', '서울시 테스트구 D동', 12, 25, 4, 'APARTMENT', '2018', '초등학교 인접', 620000000, '2026-04-01', 102.3, 74.5, NOW(), false),
    (8, '테스트 빌라 2', '서울시 테스트구 E동', 3, 5, 3, 'VILLA', '2010', '리모델링 완료', 310000000, '2026-03-10', 60.0, 46.2, NOW(), false),
    (8, '테스트 오피스텔 2', '서울시 테스트구 F동', 15, 20, 1, 'OFFICETEL', '2019', '신축급', 350000000, '2026-02-01', 42.0, 30.0, NOW(), false),

    (9, '테스트 아파트 3', '서울시 테스트구 G동', 8, 22, 3, 'APARTMENT', '2016', '공원 뷰', 520000000, '2026-05-01', 89.1, 63.0, NOW(), false),
    (9, '테스트 아파트 4', '서울시 테스트구 H동', 15, 30, 4, 'APARTMENT', '2021', '신축', 780000000, '2026-06-01', 110.0, 82.0, NOW(), false),
    (9, '테스트 빌라 3', '서울시 테스트구 I동', 1, 4, 2, 'VILLA', '2011', '마당 있음', 260000000, '2026-02-20', 50.0, 38.0, NOW(), false),
    (9, '테스트 오피스텔 3', '서울시 테스트구 J동', 7, 15, 1, 'OFFICETEL', '2017', '역 5분', 330000000, '2026-03-01', 41.0, 29.5, NOW(), false);

-- =========================
-- PropertyImages
-- =========================
INSERT INTO property_images (property_id, image_url, created_at, is_deleted) VALUES
                                                                                 (1, 'https://example.com/properties/1.jpg', NOW(), false),
                                                                                 (2, 'https://example.com/properties/2.jpg', NOW(), false),
                                                                                 (3, 'https://example.com/properties/3.jpg', NOW(), false),
                                                                                 (4, 'https://example.com/properties/4.jpg', NOW(), false),
                                                                                 (5, 'https://example.com/properties/5.jpg', NOW(), false),
                                                                                 (6, 'https://example.com/properties/6.jpg', NOW(), false),
                                                                                 (7, 'https://example.com/properties/7.jpg', NOW(), false),
                                                                                 (8, 'https://example.com/properties/8.jpg', NOW(), false),
                                                                                 (9, 'https://example.com/properties/9.jpg', NOW(), false),
                                                                                 (10,'https://example.com/properties/10.jpg', NOW(), false);

-- =========================
-- Auctions
-- =========================
INSERT INTO auctions
(property_id, start_price, status, started_at, ended_at, created_at, is_deleted)
VALUES
    (1, 320000000, 'SCHEDULED', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), NOW(), false),
    (2, 200000000, 'SCHEDULED', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), NOW(), false),
    (3, 250000000, 'SCHEDULED', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 11 DAY), NOW(), false),

    (4, 450000000, 'OPEN', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), false),
    (5, 220000000, 'OPEN', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), false),
    (6, 260000000, 'OPEN', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), NOW(), false),
    (7, 380000000, 'OPEN', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), false),

    (8, 600000000, 'CLOSED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), NOW(), false),
    (9, 180000000, 'CLOSED', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), NOW(), false),
    (10,230000000, 'CLOSED', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), NOW(), false);


-- =========================
-- Bids (GENERAL user: 1~6)
-- =========================

INSERT INTO bids
(user_id, auction_id, price, status, is_highest, created_at, is_deleted)
VALUES
-- OPEN
(1, 4, 460000000, 'OUTBID', false, NOW(), false),
(2, 4, 480000000, 'WINNING', true, NOW(), false),

(3, 5, 230000000, 'WINNING', true, NOW(), false),
(4, 5, 225000000, 'OUTBID', false, NOW(), false),

(5, 6, 270000000, 'WINNING', true, NOW(), false),
(6, 6, 265000000, 'OUTBID', false, NOW(), false),

-- CLOSED
(1, 8, 650000000, 'WON', true, NOW(), false),
(2, 8, 620000000, 'LOST', false, NOW(), false),

(3, 9, 210000000, 'WON', true, NOW(), false),
(4, 9, 200000000, 'LOST', false, NOW(), false),

(5,10, 260000000, 'WON', true, NOW(), false),
(6,10, 250000000, 'LOST', false, NOW(), false);

-- =========================
-- Auction_winners
-- =========================
INSERT INTO auction_winners
(auction_id, user_id, price, created_at, is_deleted)
VALUES
    (8, 1, 650000000, NOW(), false),
    (9, 3, 210000000, NOW(), false),
    (10,5, 260000000, NOW(), false);
