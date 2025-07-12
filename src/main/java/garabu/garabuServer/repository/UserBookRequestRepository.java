package garabu.garabuServer.repository;

import garabu.garabuServer.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBookRequestRepository extends JpaRepository<UserBookRequest, Long> {
    
    // 특정 가계부의 대기중인 요청 조회
    List<UserBookRequest> findByBookAndStatus(Book book, RequestStatus status);
    
    // 특정 사용자의 모든 요청 조회
    List<UserBookRequest> findByMemberOrderByRequestDateDesc(Member member);
    
    // 특정 사용자가 특정 가계부에 대한 대기중인 요청이 있는지 확인
    boolean existsByBookAndMemberAndStatus(Book book, Member member, RequestStatus status);
    
    // 초대 코드로 요청 찾기
    Optional<UserBookRequest> findByInviteCodeAndStatus(String inviteCode, RequestStatus status);
    
    // 특정 가계부의 모든 요청 조회 (상태별 정렬)
    @Query("SELECT r FROM UserBookRequest r WHERE r.book = :book ORDER BY " +
           "CASE r.status " +
           "WHEN 'PENDING' THEN 1 " +
           "WHEN 'ACCEPTED' THEN 2 " +
           "WHEN 'REJECTED' THEN 3 " +
           "END, r.requestDate DESC")
    List<UserBookRequest> findByBookOrderByStatusAndRequestDate(@Param("book") Book book);
} 