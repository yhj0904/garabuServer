package garabu.garabuServer.service;

import garabu.garabuServer.domain.Category;
import garabu.garabuServer.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeDefaultCategories();
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