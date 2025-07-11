package garabu.garabuServer.repository;


import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.domain.BookRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookJpaRepository extends JpaRepository<UserBook, Long> {
    /**
     * book.id를 기준으로 UserBook + Member를 한 번에 페치 조인
     * → N+1 쿼리 방지
     */
    @EntityGraph(attributePaths = {"member"})
    List<UserBook> findByBookId(Long bookId);

    boolean existsByBookId(Long bookId);
    
    @EntityGraph(attributePaths = {"member", "book"})
    Optional<UserBook> findByBookIdAndMemberId(Long bookId, Long memberId);
    
    @EntityGraph(attributePaths = {"member", "book"})
    Optional<UserBook> findByBookIdAndMemberEmail(Long bookId, String email);
    
    boolean existsByBookIdAndMemberId(Long bookId, Long memberId);
    
    boolean existsByBookIdAndMemberEmail(Long bookId, String email);
    
    @EntityGraph(attributePaths = {"book"})
    Optional<UserBook> findByBookIdAndMemberIdAndBookRole(Long bookId, Long memberId, BookRole bookRole);
    
    void deleteByBookIdAndMemberId(Long bookId, Long memberId);
    
    /**
     * 특정 멤버가 참여한 모든 가계부 조회
     */
    @EntityGraph(attributePaths = {"book"})
    List<UserBook> findByMember(garabu.garabuServer.domain.Member member);
}
