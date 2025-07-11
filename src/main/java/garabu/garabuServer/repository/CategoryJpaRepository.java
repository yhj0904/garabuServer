package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
    Category findByCategory(String category);
    
    // 가계부별 카테고리 조회
    List<Category> findByBook(Book book);
    
    // 가계부별 카테고리명으로 조회
    Category findByBookAndCategory(Book book, String category);
    
    // 기본 제공 카테고리 조회
    List<Category> findByIsDefaultTrue();
    
    // 특정 가계부의 사용자 정의 카테고리 조회
    List<Category> findByBookAndIsDefaultFalse(Book book);
    
    // 특정 사용자가 특정 가계부에 생성한 카테고리 조회
    List<Category> findByMemberAndBook(Member member, Book book);
    
    // 기본 카테고리 + 특정 가계부의 사용자 정의 카테고리 조회
    @Query("SELECT c FROM Category c WHERE c.isDefault = true OR (c.book = :book AND c.isDefault = false)")
    List<Category> findDefaultAndBookCategories(@Param("book") Book book);
}
