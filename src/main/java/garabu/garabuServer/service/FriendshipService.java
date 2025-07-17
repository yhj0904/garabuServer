package garabu.garabuServer.service;

import garabu.garabuServer.domain.Friendship;
import garabu.garabuServer.domain.FriendshipStatus;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {
    
    private final FriendshipRepository friendshipRepository;
    private final MemberService memberService;
    private final PushNotificationService pushNotificationService;
    
    /**
     * 친구 요청 보내기
     * 
     * @param requesterId 요청자 ID
     * @param addresseeId 수신자 ID
     * @param alias 요청자가 설정한 별칭
     * @return 생성된 친구 관계
     */
    @Transactional
    public Friendship sendFriendRequest(Long requesterId, Long addresseeId, String alias) {
        Member requester = memberService.findById(requesterId);
        Member addressee = memberService.findById(addresseeId);
        
        // 자기 자신에게는 친구 요청 불가
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }
        
        // 기존 친구 관계 확인
        Optional<Friendship> existingFriendship = friendshipRepository
            .findByRequesterAndAddressee(requester, addressee);
        
        if (existingFriendship.isPresent()) {
            Friendship friendship = existingFriendship.get();
            switch (friendship.getStatus()) {
                case PENDING:
                    throw new IllegalStateException("이미 친구 요청이 진행 중입니다.");
                case ACCEPTED:
                    throw new IllegalStateException("이미 친구 관계입니다.");
                case BLOCKED:
                    throw new IllegalStateException("차단된 사용자입니다.");
                case REJECTED:
                    // 거절된 경우 새로 요청 가능
                    friendship.setStatus(FriendshipStatus.PENDING);
                    friendship.setRequesterAlias(alias);
                    return friendshipRepository.save(friendship);
            }
        }
        
        // 새로운 친구 요청 생성
        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setRequesterAlias(alias);
        
        Friendship savedFriendship = friendshipRepository.save(friendship);
        
        // 푸시 알림 발송
        try {
            pushNotificationService.sendFriendRequestNotification(addresseeId, requesterId, requester.getName());
        } catch (Exception e) {
            log.error("친구 요청 알림 발송 실패", e);
        }
        
        return savedFriendship;
    }
    
    /**
     * 친구 요청 수락
     * 
     * @param friendshipId 친구 관계 ID
     * @param addresseeId 수신자 ID
     * @param alias 수신자가 설정한 별칭
     * @return 수락된 친구 관계
     */
    @Transactional
    public Friendship acceptFriendRequest(Long friendshipId, Long addresseeId, String alias) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));
        
        // 수신자 본인인지 확인
        if (!friendship.getAddressee().getId().equals(addresseeId)) {
            throw new IllegalArgumentException("친구 요청을 수락할 권한이 없습니다.");
        }
        
        // 대기 상태인지 확인
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("대기 중인 친구 요청이 아닙니다.");
        }
        
        friendship.accept();
        friendship.setAddresseeAlias(alias);
        
        Friendship savedFriendship = friendshipRepository.save(friendship);
        
        // 푸시 알림 발송
        try {
            pushNotificationService.sendFriendAcceptNotification(
                friendship.getRequester().getId(), 
                addresseeId, 
                friendship.getAddressee().getName()
            );
        } catch (Exception e) {
            log.error("친구 수락 알림 발송 실패", e);
        }
        
        return savedFriendship;
    }
    
    /**
     * 친구 요청 거절
     * 
     * @param friendshipId 친구 관계 ID
     * @param addresseeId 수신자 ID
     * @return 거절된 친구 관계
     */
    @Transactional
    public Friendship rejectFriendRequest(Long friendshipId, Long addresseeId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));
        
        // 수신자 본인인지 확인
        if (!friendship.getAddressee().getId().equals(addresseeId)) {
            throw new IllegalArgumentException("친구 요청을 거절할 권한이 없습니다.");
        }
        
        // 대기 상태인지 확인
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("대기 중인 친구 요청이 아닙니다.");
        }
        
        friendship.reject();
        return friendshipRepository.save(friendship);
    }
    
    /**
     * 친구 관계 삭제
     * 
     * @param friendshipId 친구 관계 ID
     * @param memberId 요청자 ID
     */
    @Transactional
    public void deleteFriendship(Long friendshipId, Long memberId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));
        
        // 친구 관계 당사자인지 확인
        if (!friendship.getRequester().getId().equals(memberId) && 
            !friendship.getAddressee().getId().equals(memberId)) {
            throw new IllegalArgumentException("친구 관계를 삭제할 권한이 없습니다.");
        }
        
        friendshipRepository.delete(friendship);
    }
    
    /**
     * 친구 별칭 설정
     * 
     * @param friendshipId 친구 관계 ID
     * @param memberId 요청자 ID
     * @param alias 설정할 별칭
     * @return 수정된 친구 관계
     */
    @Transactional
    public Friendship setFriendAlias(Long friendshipId, Long memberId, String alias) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));
        
        Member member = memberService.findById(memberId);
        
        // 친구 관계 당사자인지 확인
        if (!friendship.getRequester().getId().equals(memberId) && 
            !friendship.getAddressee().getId().equals(memberId)) {
            throw new IllegalArgumentException("친구 별칭을 설정할 권한이 없습니다.");
        }
        
        friendship.setAliasForMember(member, alias);
        return friendshipRepository.save(friendship);
    }
    
    /**
     * 친구 목록 조회
     * 
     * @param memberId 사용자 ID
     * @param pageable 페이징 정보
     * @return 친구 목록
     */
    public Page<Friendship> getFriends(Long memberId, Pageable pageable) {
        Member member = memberService.findById(memberId);
        return friendshipRepository.findFriendsByMemberAndStatus(member, FriendshipStatus.ACCEPTED, pageable);
    }
    
    /**
     * 받은 친구 요청 목록 조회
     * 
     * @param memberId 사용자 ID
     * @param pageable 페이징 정보
     * @return 받은 친구 요청 목록
     */
    public Page<Friendship> getReceivedFriendRequests(Long memberId, Pageable pageable) {
        Member member = memberService.findById(memberId);
        return friendshipRepository.findByAddresseeAndStatusOrderByRequestedAtDesc(
            member, FriendshipStatus.PENDING, pageable);
    }
    
    /**
     * 보낸 친구 요청 목록 조회
     * 
     * @param memberId 사용자 ID
     * @param pageable 페이징 정보
     * @return 보낸 친구 요청 목록
     */
    public Page<Friendship> getSentFriendRequests(Long memberId, Pageable pageable) {
        Member member = memberService.findById(memberId);
        return friendshipRepository.findByRequesterAndStatusOrderByRequestedAtDesc(
            member, FriendshipStatus.PENDING, pageable);
    }
    
    /**
     * 친구 수 조회
     * 
     * @param memberId 사용자 ID
     * @return 친구 수
     */
    public long getFriendCount(Long memberId) {
        Member member = memberService.findById(memberId);
        return friendshipRepository.countFriendsByMember(member);
    }
    
    /**
     * 대기 중인 친구 요청 수 조회
     * 
     * @param memberId 사용자 ID
     * @return 대기 중인 친구 요청 수
     */
    public long getPendingRequestCount(Long memberId) {
        Member member = memberService.findById(memberId);
        return friendshipRepository.countByAddresseeAndStatus(member, FriendshipStatus.PENDING);
    }
    
    /**
     * 친구 검색
     * 
     * @param memberId 사용자 ID
     * @param username 검색할 사용자명
     * @param pageable 페이징 정보
     * @return 검색된 친구 목록
     */
    public Page<Friendship> searchFriends(Long memberId, String username, Pageable pageable) {
        Member member = memberService.findById(memberId);
        return friendshipRepository.searchFriendsByUsername(member, username, pageable);
    }
    
    /**
     * 두 사용자 간의 친구 관계 조회
     * 
     * @param memberId1 사용자 1 ID
     * @param memberId2 사용자 2 ID
     * @return 친구 관계 (없으면 null)
     */
    public Optional<Friendship> getFriendship(Long memberId1, Long memberId2) {
        Member member1 = memberService.findById(memberId1);
        Member member2 = memberService.findById(memberId2);
        return friendshipRepository.findByRequesterAndAddressee(member1, member2);
    }
}