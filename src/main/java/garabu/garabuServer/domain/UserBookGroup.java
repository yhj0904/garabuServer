package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 가계부 내 사용자 그룹 관리 엔티티
 * OWNER가 가계부 참여자들을 그룹으로 관리할 수 있습니다.
 */
@Entity
@Table(name = "user_book_group")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(name = "group_name", nullable = false)
    private String groupName;
    
    @Column(name = "description")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<UserBookGroupMember> groupMembers = new ArrayList<>();
    
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
} 