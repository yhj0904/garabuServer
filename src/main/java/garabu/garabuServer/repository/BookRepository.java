package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    Book findByTitle(String Title);
    List<Book> findByOwner(Member owner);
}
