-- 1차 카테고리 (대분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('상의', NULL, 1, 0, '/1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('아우터', NULL, 2, 0, '/2', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('하의', NULL, 3, 0, '/3', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('원피스', NULL, 4, 0, '/4', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('스커트', NULL, 5, 0, '/5', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('신발', NULL, 6, 0, '/6', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('가방', NULL, 7, 0, '/7', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('패션잡화', NULL, 8, 0, '/8', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('언더웨어', NULL, 9, 0, '/9', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('홈웨어', NULL, 10, 0, '/10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 상의 하위 카테고리 (중분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('반팔 티셔츠', 1, 1, 1, '/1/101', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('긴팔 티셔츠', 1, 2, 1, '/1/102', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('셔츠/블라우스', 1, 3, 1, '/1/103', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('피케/카라 티셔츠', 1, 4, 1, '/1/104', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('후드 티셔츠', 1, 5, 1, '/1/105', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('맨투맨/스웨트셔츠', 1, 6, 1, '/1/106', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('니트/스웨터', 1, 7, 1, '/1/107', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 아우터 하위 카테고리 (중분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('후드 집업', 2, 1, 1, '/2/201', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('블루종/MA-1', 2, 2, 1, '/2/202', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('레더/라이더스 재킷', 2, 3, 1, '/2/203', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('테일러드 재킷', 2, 4, 1, '/2/204', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('카디건', 2, 5, 1, '/2/205', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('숏패딩/헤비 아우터', 2, 6, 1, '/2/206', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('코트', 2, 7, 1, '/2/207', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 하의 하위 카테고리 (중분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('데님 팬츠', 3, 1, 1, '/3/301', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('코튼 팬츠', 3, 2, 1, '/3/302', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('슈트 팬츠/슬랙스', 3, 3, 1, '/3/303', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('트레이닝/조거 팬츠', 3, 4, 1, '/3/304', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('숏 팬츠', 3, 5, 1, '/3/305', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 신발 하위 카테고리 (중분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('스니커즈', 6, 1, 1, '/6/601', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('구두', 6, 2, 1, '/6/602', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('부츠', 6, 3, 1, '/6/603', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('샌들', 6, 4, 1, '/6/604', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 스니커즈 하위 카테고리 (소분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('로우탑 스니커즈', 6, 1, 2, '/6/601/60101', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('하이탑 스니커즈', 6, 2, 2, '/6/601/60102', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('슬립온', 6, 3, 2, '/6/601/60103', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 가방 하위 카테고리 (중분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('백팩', 7, 1, 1, '/7/701', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('메신저/크로스 백', 7, 2, 1, '/7/702', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('숄더백', 7, 3, 1, '/7/703', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('토트백', 7, 4, 1, '/7/704', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 패션잡화 하위 카테고리 (중분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('모자', 8, 1, 1, '/8/801', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('양말/스타킹', 8, 2, 1, '/8/802', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('선글라스', 8, 3, 1, '/8/803', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('시계', 8, 4, 1, '/8/804', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('벨트', 8, 5, 1, '/8/805', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 모자 하위 카테고리 (소분류)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('캐주얼 모자', 8, 1, 2, '/8/801/80101', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('비니', 8, 2, 2, '/8/801/80102', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('버킷 햇', 8, 3, 2, '/8/801/80103', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 비활성화된 카테고리 (논리 삭제 테스트용)
INSERT INTO categories (name, parent_id, display_order, depth, path, is_active, created_at, updated_at) VALUES ('단종 상품', 1, 99, 1, '/1/9999', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);