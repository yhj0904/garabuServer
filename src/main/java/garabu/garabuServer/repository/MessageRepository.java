package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Message;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.friendship = :friendship AND " +
           "((m.sender = :member AND m.deletedBySender = false) OR " +
           "(m.receiver = :member AND m.deletedByReceiver = false)) " +
           "ORDER BY m.sentAt DESC")
    Page<Message> findByFriendshipAndMember(@Param("friendship") Friendship friendship,
                                           @Param("member") Member member,
                                           Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.readAt IS NULL")
    long countUnreadMessages(@Param("receiver") Member receiver);
}