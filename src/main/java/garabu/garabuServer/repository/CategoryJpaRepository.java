package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
    Category findByCategory(String category);
}
