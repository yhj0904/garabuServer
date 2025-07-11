-- 카테고리 테이블에 새로운 컬럼 추가
-- 기본 제공 카테고리와 사용자 정의 카테고리를 구분하기 위한 컬럼들

-- 이모지 컬럼 추가
ALTER TABLE category ADD COLUMN emoji VARCHAR(10) DEFAULT NULL;

-- 기본 제공 카테고리 여부 컬럼 추가
ALTER TABLE category ADD COLUMN is_default BOOLEAN DEFAULT FALSE;

-- 카테고리 생성자 컬럼 추가 (사용자 정의 카테고리의 경우)
ALTER TABLE category ADD COLUMN member_id BIGINT DEFAULT NULL;

-- member_id 외래키 제약조건 추가
ALTER TABLE category ADD CONSTRAINT fk_category_member 
FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE SET NULL;

-- 인덱스 추가
CREATE INDEX idx_category_is_default ON category(is_default);
CREATE INDEX idx_category_book_id ON category(book_id);
CREATE INDEX idx_category_member_id ON category(member_id);

-- 기본 제공 카테고리 데이터 삽입
INSERT INTO category (category, emoji, is_default, book_id, member_id) VALUES
('식비', '🍽️', TRUE, NULL, NULL),
('교통/차량', '🚗', TRUE, NULL, NULL),
('문화생활', '🎭', TRUE, NULL, NULL),
('마트/편의점', '🛒', TRUE, NULL, NULL),
('패션/미용', '👗', TRUE, NULL, NULL),
('생활용품', '🪑', TRUE, NULL, NULL),
('주거/통신', '🏠', TRUE, NULL, NULL),
('건강', '👨‍⚕️', TRUE, NULL, NULL),
('교육', '📚', TRUE, NULL, NULL),
('경조사/회비', '🎁', TRUE, NULL, NULL),
('부모님', '👨‍👩‍👧‍👦', TRUE, NULL, NULL),
('기타', '📋', TRUE, NULL, NULL),
('급여', '💰', TRUE, NULL, NULL),
('용돈', '💳', TRUE, NULL, NULL),
('투자', '📈', TRUE, NULL, NULL),
('보험', '🛡️', TRUE, NULL, NULL),
('의료', '🏥', TRUE, NULL, NULL),
('구독', '📱', TRUE, NULL, NULL),
('선물', '🎁', TRUE, NULL, NULL),
('여행', '✈️', TRUE, NULL, NULL),
('카페', '☕', TRUE, NULL, NULL),
('배달', '🚚', TRUE, NULL, NULL);