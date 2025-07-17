package garabu.garabuServer.service;

import garabu.garabuServer.domain.FriendGroup;
import garabu.garabuServer.domain.Friendship;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.FriendGroupRepository;
import garabu.garabuServer.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 친구 그룹 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FriendGroupService {
    
    private final FriendGroupRepository friendGroupRepository;
    private final FriendshipRepository friendshipRepository;
    private final MemberService memberService;
    
    /**
     * 친구 그룹 생성
     * 
     * @param ownerId 소유자 ID
     * @param name 그룹명
     * @param description 그룹 설명
     * @param color 그룹 색상
     * @param icon 그룹 아이콘
     * @return 생성된 친구 그룹
     */
    @Transactional
    public FriendGroup createFriendGroup(Long ownerId, String name, String description, 
                                        String color, String icon) {
        Member owner = memberService.findById(ownerId);
        
        // 같은 이름의 그룹이 있는지 확인
        if (friendGroupRepository.findByOwnerAndName(owner, name).isPresent()) {
            throw new IllegalArgumentException("같은 이름의 친구 그룹이 이미 존재합니다.");
        }
        
        FriendGroup friendGroup = new FriendGroup();
        friendGroup.setOwner(owner);
        friendGroup.setName(name);
        friendGroup.setDescription(description);
        friendGroup.setColor(color);
        friendGroup.setIcon(icon);
        
        return friendGroupRepository.save(friendGroup);
    }
    
    /**
     * 친구 그룹 수정
     * 
     * @param groupId 그룹 ID
     * @param ownerId 소유자 ID
     * @param name 그룹명
     * @param description 그룹 설명
     * @param color 그룹 색상
     * @param icon 그룹 아이콘
     * @return 수정된 친구 그룹
     */
    @Transactional
    public FriendGroup updateFriendGroup(Long groupId, Long ownerId, String name, String description,
                                        String color, String icon) {
        FriendGroup friendGroup = friendGroupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("친구 그룹을 찾을 수 없습니다."));
        
        // 소유자 확인
        if (!friendGroup.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("친구 그룹을 수정할 권한이 없습니다.");
        }
        
        // 그룹명 중복 확인 (현재 그룹 제외)
        if (!friendGroup.getName().equals(name)) {
            Member owner = memberService.findById(ownerId);
            if (friendGroupRepository.findByOwnerAndName(owner, name).isPresent()) {
                throw new IllegalArgumentException("같은 이름의 친구 그룹이 이미 존재합니다.");
            }
        }
        
        friendGroup.setName(name);
        friendGroup.setDescription(description);
        friendGroup.setColor(color);
        friendGroup.setIcon(icon);
        
        return friendGroupRepository.save(friendGroup);
    }
    
    /**
     * 친구 그룹 삭제
     * 
     * @param groupId 그룹 ID
     * @param ownerId 소유자 ID
     */
    @Transactional
    public void deleteFriendGroup(Long groupId, Long ownerId) {
        FriendGroup friendGroup = friendGroupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("친구 그룹을 찾을 수 없습니다."));
        
        // 소유자 확인
        if (!friendGroup.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("친구 그룹을 삭제할 권한이 없습니다.");
        }
        
        friendGroupRepository.delete(friendGroup);
    }
    
    /**
     * 친구 그룹에 친구 추가
     * 
     * @param groupId 그룹 ID
     * @param friendshipId 친구 관계 ID
     * @param ownerId 소유자 ID
     * @return 수정된 친구 그룹
     */
    @Transactional
    public FriendGroup addFriendToGroup(Long groupId, Long friendshipId, Long ownerId) {
        FriendGroup friendGroup = friendGroupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("친구 그룹을 찾을 수 없습니다."));
        
        // 소유자 확인
        if (!friendGroup.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("친구 그룹에 친구를 추가할 권한이 없습니다.");
        }
        
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));
        
        // 친구 관계 당사자인지 확인
        if (!friendship.getRequester().getId().equals(ownerId) && 
            !friendship.getAddressee().getId().equals(ownerId)) {
            throw new IllegalArgumentException("친구 관계 당사자가 아닙니다.");
        }
        
        // 이미 그룹에 포함되어 있는지 확인
        boolean alreadyInGroup = friendGroup.getMembers().stream()
            .anyMatch(member -> member.getFriendship().getId().equals(friendshipId));
        
        if (alreadyInGroup) {
            throw new IllegalArgumentException("이미 그룹에 포함된 친구입니다.");
        }
        
        friendGroup.addFriend(friendship);
        return friendGroupRepository.save(friendGroup);
    }
    
    /**
     * 친구 그룹에서 친구 제거
     * 
     * @param groupId 그룹 ID
     * @param friendshipId 친구 관계 ID
     * @param ownerId 소유자 ID
     * @return 수정된 친구 그룹
     */
    @Transactional
    public FriendGroup removeFriendFromGroup(Long groupId, Long friendshipId, Long ownerId) {
        FriendGroup friendGroup = friendGroupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("친구 그룹을 찾을 수 없습니다."));
        
        // 소유자 확인
        if (!friendGroup.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("친구 그룹에서 친구를 제거할 권한이 없습니다.");
        }
        
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));
        
        friendGroup.removeFriend(friendship);
        return friendGroupRepository.save(friendGroup);
    }
    
    /**
     * 사용자의 친구 그룹 목록 조회
     * 
     * @param ownerId 소유자 ID
     * @param pageable 페이징 정보
     * @return 친구 그룹 목록
     */
    public Page<FriendGroup> getFriendGroups(Long ownerId, Pageable pageable) {
        Member owner = memberService.findById(ownerId);
        return friendGroupRepository.findByOwnerOrderByCreatedAtDesc(owner, pageable);
    }
    
    /**
     * 사용자의 친구 그룹 목록 조회 (페이징 없음)
     * 
     * @param ownerId 소유자 ID
     * @return 친구 그룹 목록
     */
    public List<FriendGroup> getAllFriendGroups(Long ownerId) {
        Member owner = memberService.findById(ownerId);
        return friendGroupRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }
    
    /**
     * 친구 그룹 상세 조회
     * 
     * @param groupId 그룹 ID
     * @param ownerId 소유자 ID
     * @return 친구 그룹
     */
    public FriendGroup getFriendGroup(Long groupId, Long ownerId) {
        FriendGroup friendGroup = friendGroupRepository.findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("친구 그룹을 찾을 수 없습니다."));
        
        // 소유자 확인
        if (!friendGroup.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("친구 그룹을 조회할 권한이 없습니다.");
        }
        
        return friendGroup;
    }
    
    /**
     * 친구 그룹 검색
     * 
     * @param ownerId 소유자 ID
     * @param name 검색할 그룹명
     * @param pageable 페이징 정보
     * @return 검색된 친구 그룹 목록
     */
    public Page<FriendGroup> searchFriendGroups(Long ownerId, String name, Pageable pageable) {
        Member owner = memberService.findById(ownerId);
        return friendGroupRepository.searchByOwnerAndName(owner, name, pageable);
    }
    
    /**
     * 특정 친구가 속한 그룹 목록 조회
     * 
     * @param ownerId 소유자 ID
     * @param friendshipId 친구 관계 ID
     * @return 친구가 속한 그룹 목록
     */
    public List<FriendGroup> getGroupsByFriend(Long ownerId, Long friendshipId) {
        Member owner = memberService.findById(ownerId);
        return friendGroupRepository.findByOwnerAndFriendshipId(owner, friendshipId);
    }
    
    /**
     * 사용자의 친구 그룹 수 조회
     * 
     * @param ownerId 소유자 ID
     * @return 친구 그룹 수
     */
    public long getFriendGroupCount(Long ownerId) {
        Member owner = memberService.findById(ownerId);
        return friendGroupRepository.countByOwner(owner);
    }
}