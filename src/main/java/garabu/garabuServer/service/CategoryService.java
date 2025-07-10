package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;

    @CacheEvict(value = {"categories", "categoriesAll"}, allEntries = true)
    public Long rigistCategory(Category category){
        // 중복 검사는 Controller에서 처리
        categoryJpaRepository.save(category);
        return category.getId();
    }

    @Cacheable(value = "categoriesAll", unless = "#result == null or #result.isEmpty()")
    public List<Category> findAllCategories() {
        return categoryJpaRepository.findAll();
    }

    @Cacheable(value = "categories", key = "#id", unless = "#result == null")
    public Category findById(Long id) {
        return categoryJpaRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Cacheable(value = "categories", key = "#category", unless = "#result == null")
    public Category findByCategory(String category) {
        return categoryJpaRepository.findByCategory(category);
    }

}
