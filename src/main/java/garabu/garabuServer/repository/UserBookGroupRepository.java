package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.UserBookGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBookGroupRepository extends JpaRepository<UserBookGroup, Long> {
    
    // 특정 가계부의 모든 그룹 조회
    List<UserBookGroup> findByBookOrderByGroupName(Book book);
    
    // 그룹명 중복 확인
    boolean existsByBookAndGroupName(Book book, String groupName);
} 