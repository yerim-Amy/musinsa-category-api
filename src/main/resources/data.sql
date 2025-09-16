-- 1차 카테고리 (대분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (1, '상의', NULL, 1, 0, '/1', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (2, '아우터', NULL, 2, 0, '/2', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (3, '하의', NULL, 3, 0, '/3', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (4, '원피스', NULL, 4, 0, '/4', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (5, '스커트', NULL, 5, 0, '/5', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (6, '신발', NULL, 6, 0, '/6', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (7, '가방', NULL, 7, 0, '/7', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (8, '패션잡화', NULL, 8, 0, '/8', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (9, '언더웨어', NULL, 9, 0, '/9', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (10, '홈웨어', NULL, 10, 0, '/10', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 상의 하위 카테고리 (중분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (101, '반팔 티셔츠', 1, 1, 1, '/1/101', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (102, '긴팔 티셔츠', 1, 2, 1, '/1/102', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (103, '셔츠/블라우스', 1, 3, 1, '/1/103', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (104, '피케/카라 티셔츠', 1, 4, 1, '/1/104', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (105, '후드 티셔츠', 1, 5, 1, '/1/105', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (106, '맨투맨/스웨트셔츠', 1, 6, 1, '/1/106', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (107, '니트/스웨터', 1, 7, 1, '/1/107', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 아우터 하위 카테고리 (중분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (201, '후드 집업', 2, 1, 1, '/2/201', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (202, '블루종/MA-1', 2, 2, 1, '/2/202', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (203, '레더/라이더스 재킷', 2, 3, 1, '/2/203', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (204, '테일러드 재킷', 2, 4, 1, '/2/204', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (205, '카디건', 2, 5, 1, '/2/205', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (206, '숏패딩/헤비 아우터', 2, 6, 1, '/2/206', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (207, '코트', 2, 7, 1, '/2/207', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 하의 하위 카테고리 (중분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (301, '데님 팬츠', 3, 1, 1, '/3/301', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (302, '코튼 팬츠', 3, 2, 1, '/3/302', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (303, '슈트 팬츠/슬랙스', 3, 3, 1, '/3/303', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (304, '트레이닝/조거 팬츠', 3, 4, 1, '/3/304', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (305, '숏 팬츠', 3, 5, 1, '/3/305', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 신발 하위 카테고리 (중분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (601, '스니커즈', 6, 1, 1, '/6/601', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (602, '구두', 6, 2, 1, '/6/602', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (603, '부츠', 6, 3, 1, '/6/603', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (604, '샌들', 6, 4, 1, '/6/604', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 스니커즈 하위 카테고리 (소분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (60101, '로우탑 스니커즈', 601, 1, 2, '/6/601/60101', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (60102, '하이탑 스니커즈', 601, 2, 2, '/6/601/60102', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (60103, '슬립온', 601, 3, 2, '/6/601/60103', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 가방 하위 카테고리 (중분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (701, '백팩', 7, 1, 1, '/7/701', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (702, '메신저/크로스 백', 7, 2, 1, '/7/702', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (703, '숄더백', 7, 3, 1, '/7/703', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (704, '토트백', 7, 4, 1, '/7/704', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 패션잡화 하위 카테고리 (중분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (801, '모자', 8, 1, 1, '/8/801', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (802, '양말/스타킹', 8, 2, 1, '/8/802', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (803, '선글라스', 8, 3, 1, '/8/803', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (804, '시계', 8, 4, 1, '/8/804', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (805, '벨트', 8, 5, 1, '/8/805', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 모자 하위 카테고리 (소분류)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (80101, '캐주얼 모자', 801, 1, 2, '/8/801/80101', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (80102, '비니', 801, 2, 2, '/8/801/80102', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (80103, '버킷 햇', 801, 3, 2, '/8/801/80103', true, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 비활성화된 카테고리 (논리 삭제 테스트용)
INSERT INTO categories (id, name, parent_id, display_order, depth, path, is_active, created_by, created_at, updated_at) VALUES (9999, '단종 상품', 1, 99, 1, '/1/9999', false, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 새 카테고리 추가할 때 충돌 방지.시퀀스 리셋 쿼리 추가
ALTER TABLE categories ALTER COLUMN id RESTART WITH 10000;
