package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    List<Tag> findByBookId(Long bookId);
    
    Optional<Tag> findByBookIdAndName(Long bookId, String name);
    
    List<Tag> findByBookIdOrderByUsageCountDesc(Long bookId);
    
    @Query("SELECT t FROM Tag t WHERE t.book.id = :bookId ORDER BY t.usageCount DESC")
    List<Tag> findPopularTagsByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT t FROM Tag t WHERE t.book.id = :bookId AND t.name LIKE %:keyword%")
    List<Tag> searchTagsByName(@Param("bookId") Long bookId, @Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.ledgers l WHERE l.id IN :ledgerIds")
    List<Tag> findTagsByLedgerIds(@Param("ledgerIds") List<Long> ledgerIds);
    
    Optional<Tag> findByIdAndBookId(Long id, Long bookId);
    
    @Query("SELECT t FROM Tag t WHERE t.book.id = :bookId AND t.id IN :tagIds")
    List<Tag> findByBookIdAndIdIn(@Param("bookId") Long bookId, @Param("tagIds") List<Long> tagIds);
} 