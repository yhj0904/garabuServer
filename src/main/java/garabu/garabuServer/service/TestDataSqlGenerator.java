package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 테스트 데이터 SQL 스크립트 생성 서비스
 * 
 * 대량의 테스트 데이터를 SQL 스크립트로 생성하여 데이터베이스에 직접 삽입
 * 
 * @author Garabu Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestDataSqlGenerator {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final CategoryJpaRepository categoryRepository;
    private final PaymentJpaRepository paymentRepository;
    
    private final Random random = new Random();

    /**
     * Ledger 테스트 데이터 SQL 스크립트 생성
     * 
     * @param count 생성할 데이터 개수
     * @param bookId 대상 가계부 ID
     * @param outputPath 출력 파일 경로
     */
    public void generateLedgerSqlScript(int count, Long bookId, String outputPath) {
        log.info("SQL 스크립트 생성 시작 - 개수: {}, 가계부 ID: {}, 출력 경로: {}", count, bookId, outputPath);
        
        try {
            // 필요한 참조 데이터 조회
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다: " + bookId));
            
            List<Category> categories = categoryRepository.findAll();
            List<PaymentMethod> paymentMethods = paymentRepository.findAll();
            List<Member> members = memberRepository.findAll();
            
            if (categories.isEmpty() || paymentMethods.isEmpty() || members.isEmpty()) {
                throw new IllegalStateException("카테고리, 결제수단, 또는 회원 데이터가 없습니다.");
            }
            
            // SQL 스크립트 생성
            generateSqlFile(count, book, categories, paymentMethods, members, outputPath);
            
        } catch (Exception e) {
            log.error("SQL 스크립트 생성 실패", e);
            throw new RuntimeException("SQL 스크립트 생성에 실패했습니다", e);
        }
    }
    
    /**
     * SQL 파일 생성
     */
    private void generateSqlFile(int count, Book book, List<Category> categories, 
                                List<PaymentMethod> paymentMethods, List<Member> members, 
                                String outputPath) throws IOException {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            // 파일 헤더 작성
            writer.write("-- Garabu 테스트 데이터 삽입 스크립트\n");
            writer.write("-- 생성일: " + LocalDate.now() + "\n");
            writer.write("-- 대상 가계부: " + book.getTitle() + " (ID: " + book.getId() + ")\n");
            writer.write("-- 데이터 개수: " + count + "건\n");
            writer.write("-- 주의: 실행 전 데이터베이스 백업 필수!\n\n");
            
            // 트랜잭션 시작
            writer.write("START TRANSACTION;\n\n");
            
            // 배치 삽입 설정
            writer.write("-- 배치 삽입 최적화 설정\n");
            writer.write("SET autocommit = 0;\n");
            writer.write("SET unique_checks = 0;\n");
            writer.write("SET foreign_key_checks = 0;\n\n");
            
            // INSERT 문 생성
            generateInsertStatements(writer, count, book, categories, paymentMethods, members);
            
            // 설정 복원
            writer.write("\n-- 설정 복원\n");
            writer.write("SET foreign_key_checks = 1;\n");
            writer.write("SET unique_checks = 1;\n");
            writer.write("SET autocommit = 1;\n\n");
            
            // 트랜잭션 커밋
            writer.write("COMMIT;\n\n");
            
            // 통계 쿼리 추가
            writer.write("-- 삽입된 데이터 통계\n");
            writer.write("SELECT \n");
            writer.write("  '전체' as 구분,\n");
            writer.write("  COUNT(*) as 건수\n");
            writer.write("FROM ledger \n");
            writer.write("WHERE book_id = " + book.getId() + "\n");
            writer.write("UNION ALL\n");
            writer.write("SELECT \n");
            writer.write("  amount_type as 구분,\n");
            writer.write("  COUNT(*) as 건수\n");
            writer.write("FROM ledger \n");
            writer.write("WHERE book_id = " + book.getId() + "\n");
            writer.write("GROUP BY amount_type\n");
            writer.write("ORDER BY 구분;\n");
            
            log.info("SQL 스크립트 생성 완료: {}", outputPath);
        }
    }
    
    /**
     * INSERT 문 생성 (대용량 배치 처리)
     */
    private void generateInsertStatements(BufferedWriter writer, int count, Book book, 
                                        List<Category> categories, List<PaymentMethod> paymentMethods, 
                                        List<Member> members) throws IOException {
        
        int batchSize = 1000; // 한 번에 처리할 배치 크기
        
        writer.write("-- Ledger 테스트 데이터 삽입\n");
        
        for (int i = 0; i < count; i += batchSize) {
            int currentBatchSize = Math.min(batchSize, count - i);
            
            writer.write("INSERT INTO ledger (book_id, member_id, category_id, payment_id, amount_type, ");
            writer.write("spender, date, amount, description, memo) VALUES\n");
            
            for (int j = 0; j < currentBatchSize; j++) {
                // 테스트 데이터 생성
                Member member = getRandomElement(members);
                Category category = getRandomElement(categories);
                PaymentMethod paymentMethod = getRandomElement(paymentMethods);
                AmountType amountType = generateRandomAmountType();
                LocalDate date = generateRandomDate();
                Integer amount = generateRandomAmount();
                String description = generateRandomDescription(category);
                String memo = generateRandomMemo();
                String spender = generateRandomSpender();
                
                // INSERT 값 작성
                writer.write("(" + book.getId() + ", " + member.getId() + ", " + category.getId() + ", " + 
                           paymentMethod.getId() + ", '" + amountType.name() + "', ");
                writer.write("'" + escapeString(spender) + "', '" + date + "', " + amount + ", ");
                writer.write("'" + escapeString(description) + "', ");
                writer.write(memo != null ? "'" + escapeString(memo) + "'" : "NULL");
                writer.write(")");
                
                if (j < currentBatchSize - 1) {
                    writer.write(",\n");
                } else {
                    writer.write(";\n\n");
                }
            }
            
            // 진행 상황 로그
            if (i % 5000 == 0) {
                log.info("SQL 스크립트 생성 진행: {}/{}", i + currentBatchSize, count);
                writer.write("-- 진행 상황: " + (i + currentBatchSize) + "/" + count + " 완료\n");
            }
        }
    }
    
    /**
     * 랜덤 AmountType 생성
     */
    private AmountType generateRandomAmountType() {
        int rand = random.nextInt(100);
        if (rand < 70) {
            return AmountType.EXPENSE;
        } else if (rand < 90) {
            return AmountType.INCOME;
        } else {
            return AmountType.TRANSFER;
        }
    }
    
    /**
     * 랜덤 날짜 생성 (최근 2년)
     */
    private LocalDate generateRandomDate() {
        LocalDate now = LocalDate.now();
        LocalDate twoYearsAgo = now.minusYears(2);
        long daysBetween = twoYearsAgo.toEpochDay() - now.toEpochDay();
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween);
        return now.plusDays(randomDays);
    }
    
    /**
     * 랜덤 금액 생성
     */
    private Integer generateRandomAmount() {
        int[] amounts = {
            1000, 2000, 3000, 5000, 10000, 15000, 20000, 30000, 50000, 100000,
            150000, 200000, 300000, 500000
        };
        
        int baseAmount = amounts[random.nextInt(amounts.length)];
        int variation = random.nextInt(5000) - 2500;
        return Math.max(1000, baseAmount + variation);
    }
    
    /**
     * 카테고리 기반 랜덤 설명 생성
     */
    private String generateRandomDescription(Category category) {
        String[] descriptions = {
            category.getCategory() + " 관련 지출",
            "정기 " + category.getCategory(),
            category.getCategory() + " 구매",
            "월간 " + category.getCategory() + " 비용",
            category.getCategory() + " 결제",
            "기타 " + category.getCategory() + " 항목"
        };
        
        return descriptions[random.nextInt(descriptions.length)];
    }
    
    /**
     * 랜덤 메모 생성
     */
    private String generateRandomMemo() {
        String[] memos = {
            "정기 지출",
            "필수 구매",
            "할인 혜택",
            "온라인 구매",
            "카드 결제",
            "현금 결제",
            "이벤트 참여",
            "멤버십 혜택",
            null, null, null  // 30% 확률로 메모 없음
        };
        
        return memos[random.nextInt(memos.length)];
    }
    
    /**
     * 랜덤 지출자 생성
     */
    private String generateRandomSpender() {
        String[] spenders = {
            "아버지", "어머니", "형/누나", "동생", "본인", "기타 가족"
        };
        
        return spenders[random.nextInt(spenders.length)];
    }
    
    /**
     * 리스트에서 랜덤 요소 선택
     */
    private <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * SQL 문자열 이스케이프 처리
     */
    private String escapeString(String str) {
        if (str == null) return null;
        return str.replace("'", "''").replace("\\", "\\\\");
    }
    
    /**
     * 여러 테이블의 테스트 데이터 SQL 스크립트 생성
     */
    public void generateCompleteTestDataSql(String outputPath) {
        log.info("완전한 테스트 데이터 SQL 스크립트 생성 시작: {}", outputPath);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            // 파일 헤더
            writer.write("-- Garabu 완전한 테스트 데이터 삽입 스크립트\n");
            writer.write("-- 생성일: " + LocalDate.now() + "\n");
            writer.write("-- 주의: 실행 전 데이터베이스 백업 필수!\n\n");
            
            // 기본 설정
            writer.write("START TRANSACTION;\n");
            writer.write("SET autocommit = 0;\n");
            writer.write("SET unique_checks = 0;\n");
            writer.write("SET foreign_key_checks = 0;\n\n");
            
            // 설정 복원 및 커밋
            writer.write("SET foreign_key_checks = 1;\n");
            writer.write("SET unique_checks = 1;\n");
            writer.write("SET autocommit = 1;\n");
            writer.write("COMMIT;\n\n");
            
            log.info("완전한 테스트 데이터 SQL 스크립트 생성 완료: {}", outputPath);
            
        } catch (Exception e) {
            log.error("완전한 테스트 데이터 SQL 스크립트 생성 실패", e);
            throw new RuntimeException("SQL 스크립트 생성에 실패했습니다", e);
        }
    }
}