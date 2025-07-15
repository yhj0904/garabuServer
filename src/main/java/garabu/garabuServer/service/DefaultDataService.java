package garabu.garabuServer.service;

import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.domain.UserRole;
import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Payment;
import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.repository.CategoryJpaRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.BookJpaRepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import garabu.garabuServer.repository.LedgerJpaRepository;
import garabu.garabuServer.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * 기본 데이터 초기화 서비스
 * 
 * 애플리케이션 시작 시 기본 카테고리 데이터를 생성합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultDataService implements CommandLineRunner {

    private final CategoryJpaRepository categoryRepository;
    private final MemberJPARepository memberRepository;
    private final BookJpaRepository bookRepository;
    private final UserBookJpaRepository userBookRepository;
    private final LedgerJpaRepository ledgerRepository;
    private final PaymentJpaRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeDefaultCategories();
        initializeTestAccounts();
    }

    /**
     * 테스트 계정 초기화
     */
    private void initializeTestAccounts() {
        String testEmail = "test1@garabu.com";
        
        // 이미 테스트 계정이 존재하는지 확인
        if (memberRepository.findByEmail(testEmail).size() > 0) {
            log.info("테스트 계정이 이미 존재합니다.");
            return;
        }

        log.info("테스트 계정을 생성합니다.");

        // 테스트 계정 생성
        Member testMember = new Member();
        testMember.setUsername("testuser1");
        testMember.setEmail(testEmail);
        testMember.setName("테스트 유저");
        testMember.setPassword(passwordEncoder.encode("test1"));
        testMember.setSystemRole(garabu.garabuServer.domain.SystemRole.ROLE_USER);
        testMember.setProviderId(null); // LOCAL 계정은 providerId가 null
        
        memberRepository.save(testMember);
        log.info("테스트 계정이 생성되었습니다: {}", testEmail);
        
        // 테스트 가계부 생성
        createTestBooksAndData(testMember);
    }

    /**
     * 테스트 가계부와 데이터 생성
     */
    private void createTestBooksAndData(Member testMember) {
        // 개인 가계부 생성
        Book personalBook = new Book();
        personalBook.setTitle("나의 가계부");
        personalBook.setOwnerId(testMember.getId());
        bookRepository.save(personalBook);
        
        // UserBook 관계 설정
        UserBook userBook = new UserBook();
        userBook.setMember(testMember);
        userBook.setBook(personalBook);
        userBook.setUserRole(UserRole.OWNER);
        userBookRepository.save(userBook);
        
        // 기본 결제수단 생성
        createDefaultPayments(personalBook);
        
        // 샘플 거래 내역 생성
        createSampleLedgers(personalBook, testMember);
        
        log.info("테스트 가계부와 샘플 데이터가 생성되었습니다.");
    }
    
    /**
     * 기본 결제수단 생성
     */
    private void createDefaultPayments(Book book) {
        List<String> payments = List.of("현금", "신용카드", "체크카드", "계좌이체", "페이");
        
        for (String paymentName : payments) {
            Payment payment = new Payment();
            payment.setPayment(paymentName);
            payment.setBook(book);
            paymentRepository.save(payment);
        }
    }
    
    /**
     * 샘플 거래 내역 생성
     */
    private void createSampleLedgers(Book book, Member member) {
        List<Category> categories = categoryRepository.findAll();
        List<Payment> payments = paymentRepository.findByBook(book);
        Random random = new Random();
        
        // 최근 30일간의 샘플 데이터 생성
        for (int i = 0; i < 30; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            
            // 하루에 1-3개의 거래 생성
            int transactionCount = random.nextInt(3) + 1;
            for (int j = 0; j < transactionCount; j++) {
                createRandomLedger(book, member, categories, payments, date, random);
            }
        }
        
        // 수입 데이터도 추가 (월급, 용돈 등)
        createIncomeLedgers(book, member, categories, payments);
    }
    
    /**
     * 랜덤 지출 거래 생성
     */
    private void createRandomLedger(Book book, Member member, List<Category> categories, 
                                   List<Payment> payments, LocalDate date, Random random) {
        Ledger ledger = new Ledger();
        ledger.setBook(book);
        ledger.setMember(member);
        ledger.setDate(date);
        ledger.setAmountType(AmountType.EXPENSE);
        
        // 카테고리별 지출 패턴
        Category category = categories.get(random.nextInt(categories.size()));
        ledger.setCategory(category);
        
        // 카테고리에 따른 금액 설정
        int amount = getAmountByCategory(category.getCategory(), random);
        ledger.setAmount(amount);
        
        // 설명 설정
        ledger.setDescription(getDescriptionByCategory(category.getCategory(), random));
        
        // 결제수단 설정
        ledger.setPayment(payments.get(random.nextInt(payments.size())));
        
        // 지출자
        ledger.setSpender(member.getName());
        
        ledgerRepository.save(ledger);
    }
    
    /**
     * 수입 거래 생성
     */
    private void createIncomeLedgers(Book book, Member member, List<Category> categories, List<Payment> payments) {
        // 월급
        Category salaryCategory = categories.stream()
            .filter(c -> c.getCategory().equals("급여"))
            .findFirst()
            .orElse(categories.get(0));
            
        Ledger salary = new Ledger();
        salary.setBook(book);
        salary.setMember(member);
        salary.setDate(LocalDate.now().withDayOfMonth(1));
        salary.setAmountType(AmountType.INCOME);
        salary.setCategory(salaryCategory);
        salary.setAmount(3000000);
        salary.setDescription("월급");
        salary.setPayment(payments.get(0));
        salary.setSpender(member.getName());
        ledgerRepository.save(salary);
        
        // 부수입
        Category extraCategory = categories.stream()
            .filter(c -> c.getCategory().equals("기타"))
            .findFirst()
            .orElse(categories.get(0));
            
        Ledger extra = new Ledger();
        extra.setBook(book);
        extra.setMember(member);
        extra.setDate(LocalDate.now().minusDays(15));
        extra.setAmountType(AmountType.INCOME);
        extra.setCategory(extraCategory);
        extra.setAmount(500000);
        extra.setDescription("프리랜서 수입");
        extra.setPayment(payments.get(0));
        extra.setSpender(member.getName());
        ledgerRepository.save(extra);
    }
    
    /**
     * 카테고리별 금액 생성
     */
    private int getAmountByCategory(String category, Random random) {
        return switch (category) {
            case "식비" -> (random.nextInt(3) + 1) * 10000;
            case "교통/차량" -> (random.nextInt(5) + 1) * 5000;
            case "문화생활" -> (random.nextInt(10) + 1) * 10000;
            case "마트/편의점" -> (random.nextInt(5) + 1) * 10000;
            case "카페" -> (random.nextInt(3) + 1) * 5000;
            case "배달" -> (random.nextInt(4) + 2) * 10000;
            default -> (random.nextInt(10) + 1) * 10000;
        };
    }
    
    /**
     * 카테고리별 설명 생성
     */
    private String getDescriptionByCategory(String category, Random random) {
        List<String> descriptions = switch (category) {
            case "식비" -> List.of("점심 식사", "저녁 식사", "아침 식사", "간식");
            case "교통/차량" -> List.of("지하철", "버스", "택시", "주유");
            case "문화생활" -> List.of("영화", "공연", "전시회", "도서 구매");
            case "마트/편의점" -> List.of("생필품 구매", "장보기", "편의점", "마트");
            case "카페" -> List.of("커피", "디저트", "카페", "음료");
            case "배달" -> List.of("배달음식", "야식", "치킨", "피자");
            default -> List.of("기타 지출");
        };
        
        return descriptions.get(random.nextInt(descriptions.size()));
    }

    /**
     * 기본 카테고리 데이터 초기화
     */
    private void initializeDefaultCategories() {
        // 기본 카테고리가 이미 존재하는지 확인
        if (categoryRepository.existsByIsDefaultTrue()) {
            log.info("기본 카테고리가 이미 존재합니다.");
            return;
        }

        log.info("기본 카테고리 데이터를 초기화합니다.");

        List<Category> defaultCategories = List.of(
            new Category("식비", "🍽️", true),
            new Category("교통/차량", "🚗", true),
            new Category("문화생활", "🎭", true),
            new Category("마트/편의점", "🛒", true),
            new Category("패션/미용", "👗", true),
            new Category("생활용품", "🪑", true),
            new Category("주거/통신", "🏠", true),
            new Category("건강", "👨‍⚕️", true),
            new Category("교육", "📚", true),
            new Category("경조사/회비", "🎁", true),
            new Category("부모님", "👨‍👩‍👧‍👦", true),
            new Category("기타", "📋", true),
            new Category("급여", "💰", true),
            new Category("용돈", "💳", true),
            new Category("투자", "📈", true),
            new Category("보험", "🛡️", true),
            new Category("의료", "🏥", true),
            new Category("구독", "📱", true),
            new Category("선물", "🎁", true),
            new Category("여행", "✈️", true),
            new Category("카페", "☕", true),
            new Category("배달", "🚚", true)
        );

        categoryRepository.saveAll(defaultCategories);
        log.info("기본 카테고리 {} 개가 생성되었습니다.", defaultCategories.size());
    }
}