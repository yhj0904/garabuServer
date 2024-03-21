package garabu.garabuServer.service;

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

}
