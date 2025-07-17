package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Comment;
import garabu.garabuServer.domain.CommentType;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Ledger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT c FROM Comment c WHERE c.book = :book AND c.commentType = :type AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByBookAndCommentType(@Param("book") Book book,
                                          @Param("type") CommentType type,
                                          Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.ledger = :ledger AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findByLedger(@Param("ledger") Ledger ledger, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.book = :book AND c.commentType = :type AND c.deletedAt IS NULL")
    long countByBookAndCommentType(@Param("book") Book book, @Param("type") CommentType type);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.ledger = :ledger AND c.deletedAt IS NULL")
    long countByLedger(@Param("ledger") Ledger ledger);
}