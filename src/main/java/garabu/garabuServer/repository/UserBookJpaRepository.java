package garabu.garabuServer.repository;


import garabu.garabuServer.domain.UserBook;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBookJpaRepository extends JpaRepository<UserBook, Long> {
    /**
     * book.id를 기준으로 UserBook + Member를 한 번에 페치 조인
     * → N+1 쿼리 방지
     */
    @EntityGraph(attributePaths = {"member"})
    List<UserBook> findByBookId(Long bookId);

    boolean existsByBookId(Long bookId);
}
