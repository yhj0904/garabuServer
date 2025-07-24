package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Memo;
import garabu.garabuServer.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
    
    /**
     * 가계부로 메모 조회
     * 
     * @param book 가계부
     * @return 메모 (Optional)
     */
    Optional<Memo> findByBook(Book book);
    
    /**
     * 가계부 ID로 메모 조회
     * 
     * @param bookId 가계부 ID
     * @return 메모 (Optional)
     */
    Optional<Memo> findByBookId(Long bookId);
    
    /**
     * 가계부에 메모가 존재하는지 확인
     * 
     * @param book 가계부
     * @return 존재 여부
     */
    boolean existsByBook(Book book);
}