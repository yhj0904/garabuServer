package garabu.garabuServer.repository;

import garabu.garabuServer.domain.FriendGroup;
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
 * 친구 그룹 Repository
 */
@Repository
public interface FriendGroupRepository extends JpaRepository<FriendGroup, Long> {
    
    /**
     * 소유자의 친구 그룹 목록 조회
     * 
     * @param owner 소유자
     * @param pageable 페이징 정보
     * @return 친구 그룹 목록
     */
    Page<FriendGroup> findByOwnerOrderByCreatedAtDesc(Member owner, Pageable pageable);
    
    /**
     * 소유자의 친구 그룹 목록 조회 (페이징 없음)
     * 
     * @param owner 소유자
     * @return 친구 그룹 목록
     */
    List<FriendGroup> findByOwnerOrderByCreatedAtDesc(Member owner);
    
    /**
     * 소유자와 그룹명으로 친구 그룹 조회
     * 
     * @param owner 소유자
     * @param name 그룹명
     * @return 친구 그룹
     */
    Optional<FriendGroup> findByOwnerAndName(Member owner, String name);
    
    /**
     * 소유자의 친구 그룹 수 조회
     * 
     * @param owner 소유자
     * @return 친구 그룹 수
     */
    long countByOwner(Member owner);
    
    /**
     * 그룹명으로 친구 그룹 검색
     * 
     * @param owner 소유자
     * @param name 검색할 그룹명
     * @param pageable 페이징 정보
     * @return 검색된 친구 그룹 목록
     */
    @Query("SELECT fg FROM FriendGroup fg WHERE fg.owner = :owner AND fg.name LIKE %:name%")
    Page<FriendGroup> searchByOwnerAndName(@Param("owner") Member owner,
                                          @Param("name") String name,
                                          Pageable pageable);
    
    /**
     * 특정 친구 관계가 포함된 그룹 목록 조회
     * 
     * @param owner 소유자
     * @param friendshipId 친구 관계 ID
     * @return 친구 그룹 목록
     */
    @Query("SELECT fg FROM FriendGroup fg " +
           "JOIN fg.members m " +
           "WHERE fg.owner = :owner AND m.friendship.id = :friendshipId")
    List<FriendGroup> findByOwnerAndFriendshipId(@Param("owner") Member owner,
                                                @Param("friendshipId") Long friendshipId);
}