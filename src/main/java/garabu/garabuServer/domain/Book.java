package garabu.garabuServer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 가계부 엔티티
 *
 * 사용자의 가계부(개인, 커플, 모임 등)를 관리하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "가계부 엔티티")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    @Schema(description = "가계부 식별자")
    private Long id;                // 가계부 식별자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(description = "가계부 소유 회원")
    private Member owner;      // 소유한 사용자의 식별자 Id

    @Schema(description = "가계부 이름", example = "개인 가계부")
    private String title;   // 가계부 이름 ex)개인 가계부, 커플 가계부

    @OneToMany(mappedBy = "book")
    @JsonManagedReference("book-userBooks")
    @Schema(description = "가계부에 속한 사용자 목록")
    private List<UserBook> userBooks = new ArrayList<>();

    @Column(length = 3, nullable = false)
    @Schema(description = "기본 통화 코드", example = "KRW")
    private String defaultCurrency = "KRW";
    
    @Column(nullable = false)
    @Schema(description = "다중 통화 사용 여부")
    private Boolean useMultiCurrency = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    public boolean hasMember(Member member) {
        if (member == null) return false;
        
        // 소유자인 경우
        if (owner != null && owner.equals(member)) {
            return true;
        }
        
        // UserBook을 통해 연결된 멤버인 경우
        return userBooks.stream()
                .anyMatch(ub -> ub.getMember() != null && ub.getMember().equals(member));
    }
}
