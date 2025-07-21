package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.DefaultCategory;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.dto.CategoryDto;
import garabu.garabuServer.exception.BookAccessException;
import garabu.garabuServer.exception.InsufficientPermissionException;
import garabu.garabuServer.repository.CategoryJpaRepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;
    private final MemberService memberService;
    private final UserBookJpaRepository userBookJpaRepository;

    @CacheEvict(value = {"categoriesAllDto", "categoriesByBookDto", "combinedCategoriesDto", "userCategoriesDto"}, allEntries = true)
    public Long rigistCategory(Category category){
        // 중복 검사는 Controller에서 처리
        categoryJpaRepository.save(category);
        return category.getId();
    }

    // Deprecated: Entity caching removed to prevent LazyInitializationException
    // Use findAllCategoriesDto() instead
    public List<Category> findAllCategories() {
        return categoryJpaRepository.findAll();
    }
    
    // DTO 기반 캐싱 - 모든 카테고리 조회
    @Cacheable(value = "categoriesAllDto", unless = "#result == null or #result.isEmpty()")
    public List<CategoryDto> findAllCategoriesDto() {
        List<Category> entities = categoryJpaRepository.findAll();
        return entities.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }

    // Deprecated: Entity caching removed to prevent LazyInitializationException
    public Category findById(Long id) {
        return categoryJpaRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // Deprecated: Entity caching removed to prevent LazyInitializationException
    public Category findByCategory(String category) {
        return categoryJpaRepository.findByCategory(category);
    }

    // 가계부별 카테고리 조회
    // Deprecated: Entity caching removed to prevent LazyInitializationException
    // Use findByBookDto() instead
    public List<Category> findByBook(Book book) {
        return categoryJpaRepository.findByBook(book);
    }
    
    // DTO 기반 캐싱 - 가계부별 카테고리 조회
    @Cacheable(value = "categoriesByBookDto", key = "'book_' + #book.id", unless = "#result == null or #result.isEmpty()")
    public List<CategoryDto> findByBookDto(Book book) {
        List<Category> entities = categoryJpaRepository.findByBook(book);
        return entities.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }

    // 가계부별 카테고리명으로 조회 (기본 카테고리 포함)
    public Category findByBookAndCategory(Book book, String category) {
        return categoryJpaRepository.findByBookAndCategoryIncludingDefault(book, category);
    }

    // 가계부별 카테고리 생성
    @CacheEvict(value = {"categoriesByBookDto", "combinedCategoriesDto", "userCategoriesDto"}, allEntries = true)
    public Long createCategoryForBook(Book book, String categoryName) {
        Member currentMember = memberService.getCurrentMember();
        Category category = new Category();
        category.setCategory(categoryName);
        category.setBook(book);
        category.setMember(currentMember);
        category.setIsDefault(false);
        categoryJpaRepository.save(category);
        return category.getId();
    }
    
    // 기본 제공 카테고리 조회
    // Deprecated: Entity caching removed to prevent LazyInitializationException
    // Use findDefaultCategoriesDto() instead
    public List<Category> findDefaultCategories() {
        return categoryJpaRepository.findByIsDefaultTrue();
    }
    
    // DTO 기반 캐싱 - 기본 카테고리 조회
    @Cacheable(value = "defaultCategoriesDto", unless = "#result == null or #result.isEmpty()")
    public List<CategoryDto> findDefaultCategoriesDto() {
        List<Category> entities = categoryJpaRepository.findByIsDefaultTrue();
        return entities.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }
    
    // 기본 카테고리 + 가계부별 사용자 정의 카테고리 조회
    // Deprecated: Entity caching removed to prevent LazyInitializationException
    // Use findCombinedCategoriesDto() instead
    public List<Category> findCombinedCategories(Book book) {
        return categoryJpaRepository.findDefaultAndBookCategories(book);
    }
    
    // DTO 기반 캐싱 - 기본 + 가계부별 카테고리 조회
    @Cacheable(value = "combinedCategoriesDto", key = "'combined_' + #book.id", unless = "#result == null or #result.isEmpty()")
    public List<CategoryDto> findCombinedCategoriesDto(Book book) {
        List<Category> entities = categoryJpaRepository.findDefaultAndBookCategories(book);
        return entities.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }
    
    // 기본 카테고리 초기화 (애플리케이션 시작 시 호출)
    @CacheEvict(value = {"defaultCategoriesDto", "combinedCategoriesDto", "categoriesAllDto", "categoriesByBookDto", "userCategoriesDto"}, allEntries = true)
    public void initializeDefaultCategories() {
        // 기본 카테고리가 이미 존재하는지 확인
        List<Category> existingDefaults = categoryJpaRepository.findByIsDefaultTrue();
        if (!existingDefaults.isEmpty()) {
            return; // 이미 존재하면 스킵
        }
        
        // 기본 카테고리 생성
        List<Category> defaultCategories = new ArrayList<>();
        for (DefaultCategory defaultCat : DefaultCategory.values()) {
            Category category = new Category();
            category.setCategory(defaultCat.getDisplayName());
            category.setEmoji(defaultCat.getEmoji());
            category.setIsDefault(true);
            category.setBook(null);
            category.setMember(null);
            defaultCategories.add(category);
        }
        
        categoryJpaRepository.saveAll(defaultCategories);
    }
    
    // 가계부별 사용자 정의 카테고리만 조회
    // Deprecated: Entity caching removed to prevent LazyInitializationException
    public List<Category> findUserCategoriesByBook(Book book) {
        return categoryJpaRepository.findByBookAndIsDefaultFalse(book);
    }
    
    // DTO 기반 캐싱 - 가계부별 사용자 정의 카테고리 조회
    @Cacheable(value = "userCategoriesDto", key = "'user_' + #book.id", unless = "#result == null or #result.isEmpty()")
    public List<CategoryDto> findUserCategoriesByBookDto(Book book) {
        List<Category> entities = categoryJpaRepository.findByBookAndIsDefaultFalse(book);
        return entities.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 가계부 읽기 권한 검증
     * 모든 역할(OWNER, EDITOR, VIEWER)이 읽기 권한을 가짐
     */
    public void validateBookAccess(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("가계부 정보가 없습니다.");
        }
        
        Member currentMember = memberService.getCurrentMember();
        Optional<UserBook> userBook = userBookJpaRepository.findByBookIdAndMemberId(book.getId(), currentMember.getId());
        
        if (userBook.isEmpty()) {
            throw new BookAccessException("해당 가계부에 대한 접근 권한이 없습니다. 가계부 ID: " + book.getId());
        }
    }
    
    /**
     * 가계부 편집 권한 검증
     * OWNER와 EDITOR만 편집 권한을 가짐 (VIEWER는 읽기 전용)
     */
    public void validateBookEditAccess(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("가계부 정보가 없습니다.");
        }
        
        Member currentMember = memberService.getCurrentMember();
        Optional<UserBook> userBookOpt = userBookJpaRepository.findByBookIdAndMemberId(book.getId(), currentMember.getId());
        
        if (userBookOpt.isEmpty()) {
            throw new BookAccessException("해당 가계부에 대한 접근 권한이 없습니다. 가계부 ID: " + book.getId());
        }
        
        UserBook userBook = userBookOpt.get();
        BookRole role = userBook.getBookRole();
        
        if (role == BookRole.VIEWER) {
            throw new InsufficientPermissionException(
                "카테고리를 추가하려면 편집 권한이 필요합니다. 현재 권한: " + role + 
                ", 필요 권한: OWNER 또는 EDITOR"
            );
        }
    }
    
    /**
     * 사용자의 가계부 역할 조회
     */
    public BookRole getUserBookRole(Book book, Member member) {
        Optional<UserBook> userBook = userBookJpaRepository.findByBookIdAndMemberId(book.getId(), member.getId());
        return userBook.map(UserBook::getBookRole).orElse(null);
    }
    
    /**
     * 사용자가 특정 권한을 가지고 있는지 확인
     */
    public boolean hasPermission(Book book, Member member, BookRole requiredRole) {
        BookRole userRole = getUserBookRole(book, member);
        if (userRole == null) {
            return false;
        }
        
        // 권한 계층: OWNER > EDITOR > VIEWER
        return switch (requiredRole) {
            case VIEWER -> true; // 모든 권한이 VIEWER 이상
            case EDITOR -> userRole == BookRole.OWNER || userRole == BookRole.EDITOR;
            case OWNER -> userRole == BookRole.OWNER;
        };
    }
    
    /**
     * 모든 카테고리 관련 캐시를 초기화합니다.
     * Redis 직렬화 문제 발생 시 호출하여 캐시를 완전히 초기화합니다.
     */
    @CacheEvict(value = {"categories", "categoriesAll", "categoriesByBook", "combinedCategories", 
                         "categoriesAllDto", "categoriesByBookDto", "combinedCategoriesDto", 
                         "userCategoriesDto", "defaultCategories", "defaultCategoriesDto"}, allEntries = true)
    public void clearAllCategoryCaches() {
        // 캐시 초기화만 수행
    }
    
    // 특정 사용자가 특정 가계부에 생성한 카테고리 조회
    public List<Category> findCategoriesByMemberAndBook(Member member, Book book) {
        return categoryJpaRepository.findByMemberAndBook(member, book);
    }
}
