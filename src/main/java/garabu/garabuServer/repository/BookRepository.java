package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Book findByName(String name);
}
