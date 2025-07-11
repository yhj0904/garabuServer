package garabu.garabuServer.config;

import garabu.garabuServer.service.CategoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 시작 시 기본 카테고리 초기화
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CategoryInitializer {

    private final CategoryService categoryService;

    /**
     * 애플리케이션 시작 시 기본 카테고리 초기화
     */
    @PostConstruct
    public void initializeDefaultCategories() {
        log.info("기본 카테고리 초기화 시작");
        
        try {
            categoryService.initializeDefaultCategories();
            log.info("기본 카테고리 초기화 완료");
        } catch (Exception e) {
            log.error("기본 카테고리 초기화 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}