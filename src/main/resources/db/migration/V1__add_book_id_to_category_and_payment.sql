-- 카테고리 테이블에 book_id 컬럼 추가
ALTER TABLE category ADD COLUMN book_id BIGINT;

-- 결제수단 테이블에 book_id 컬럼 추가
ALTER TABLE payment ADD COLUMN book_id BIGINT;

-- 외래키 제약조건 추가
ALTER TABLE category ADD CONSTRAINT fk_category_book FOREIGN KEY (book_id) REFERENCES book(book_id);
ALTER TABLE payment ADD CONSTRAINT fk_payment_book FOREIGN KEY (book_id) REFERENCES book(book_id);

-- 기존 데이터가 있다면 기본 가계부로 설정 (선택사항)
-- UPDATE category SET book_id = (SELECT book_id FROM book LIMIT 1) WHERE book_id IS NULL;
-- UPDATE payment SET book_id = (SELECT book_id FROM book LIMIT 1) WHERE book_id IS NULL; 