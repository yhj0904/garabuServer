package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 가계부 그룹 멤버 관리 엔티티
 */
@Entity
@Table(name = "user_book_group_member")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookGroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private UserBookGroup group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_book_id", nullable = false)
    private UserBook userBook;
    
    @Column(name = "joined_date", nullable = false)
    private LocalDateTime joinedDate;
    
    @PrePersist
    public void prePersist() {
        this.joinedDate = LocalDateTime.now();
    }
} 