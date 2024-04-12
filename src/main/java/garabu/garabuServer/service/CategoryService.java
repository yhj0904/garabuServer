package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;

    public Long rigistCategory(Category category){

        categoryJpaRepository.save(category);
        return category.getId();
    }

    public Category findById(Long id) {
        return categoryJpaRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Category findByCategory(String category) {
        return categoryJpaRepository.findByCategory(category);
    }

}
