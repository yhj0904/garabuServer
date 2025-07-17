package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Friendship;
import garabu.garabuServer.domain.FriendshipStatus;
import garabu.garabuServer.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 Repository
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    /**
     * 두 사용자 간의 친구 관계 조회
     * 
     * @param requester 요청자
     * @param addressee 수신자
     * @return 친구 관계 (양방향 검색)
     */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :requester AND f.addressee = :addressee) OR " +
           "(f.requester = :addressee AND f.addressee = :requester)")
    Optional<Friendship> findByRequesterAndAddressee(@Param("requester") Member requester, 
                                                   @Param("addressee") Member addressee);
    
    /**
     * 사용자의 친구 목록 조회 (수락된 친구만)
     * 
     * @param member 사용자
     * @param pageable 페이징 정보
     * @return 친구 목록
     */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :member OR f.addressee = :member) AND " +
           "f.status = :status " +
           "ORDER BY f.lastInteractionAt DESC")
    Page<Friendship> findFriendsByMemberAndStatus(@Param("member") Member member,
                                                 @Param("status") FriendshipStatus status,
                                                 Pageable pageable);
    
    /**
     * 사용자가 보낸 친구 요청 목록 조회
     * 
     * @param requester 요청자
     * @param status 상태
     * @param pageable 페이징 정보
     * @return 친구 요청 목록
     */
    Page<Friendship> findByRequesterAndStatusOrderByRequestedAtDesc(Member requester,
                                                                   FriendshipStatus status,
                                                                   Pageable pageable);
    
    /**
     * 사용자가 받은 친구 요청 목록 조회
     * 
     * @param addressee 수신자
     * @param status 상태
     * @param pageable 페이징 정보
     * @return 친구 요청 목록
     */
    Page<Friendship> findByAddresseeAndStatusOrderByRequestedAtDesc(Member addressee,
                                                                   FriendshipStatus status,
                                                                   Pageable pageable);
    
    /**
     * 사용자의 친구 수 조회
     * 
     * @param member 사용자
     * @return 친구 수
     */
    @Query("SELECT COUNT(f) FROM Friendship f WHERE " +
           "(f.requester = :member OR f.addressee = :member) AND " +
           "f.status = 'ACCEPTED'")
    long countFriendsByMember(@Param("member") Member member);
    
    /**
     * 사용자의 대기 중인 친구 요청 수 조회
     * 
     * @param addressee 수신자
     * @return 대기 중인 친구 요청 수
     */
    long countByAddresseeAndStatus(Member addressee, FriendshipStatus status);
    
    /**
     * 사용자의 친구 관계 목록 조회 (상태별)
     * 
     * @param member 사용자
     * @param status 상태
     * @return 친구 관계 목록
     */
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester = :member OR f.addressee = :member) AND " +
           "f.status = :status")
    List<Friendship> findByMemberAndStatus(@Param("member") Member member,
                                          @Param("status") FriendshipStatus status);
    
    /**
     * 사용자명으로 친구 검색
     * 
     * @param currentMember 현재 사용자
     * @param username 검색할 사용자명
     * @param pageable 페이징 정보
     * @return 검색된 친구 목록
     */
    @Query("SELECT f FROM Friendship f " +
           "JOIN Member m ON (f.requester = m OR f.addressee = m) " +
           "WHERE (f.requester = :currentMember OR f.addressee = :currentMember) " +
           "AND f.status = 'ACCEPTED' " +
           "AND m != :currentMember " +
           "AND (m.username LIKE %:username% OR m.name LIKE %:username%)")
    Page<Friendship> searchFriendsByUsername(@Param("currentMember") Member currentMember,
                                            @Param("username") String username,
                                            Pageable pageable);
}