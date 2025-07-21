package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags",
    indexes = {
        @Index(name = "idx_tag_book", columnList = "book_id"),
        @Index(name = "idx_tag_name", columnList = "book_id, name")
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 7)
    private String color; // 헥스 색상 코드
    
    @Column(nullable = false)
    private Integer usageCount = 0;
    
    @ManyToMany(mappedBy = "tags")
    private Set<Ledger> ledgers = new HashSet<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 생성자
    public Tag(Book book, String name, String color) {
        this.book = book;
        this.name = name;
        this.color = color != null ? color : "#000000";
        this.usageCount = 0;
    }
    
    // 비즈니스 메서드
    public void addLedger(Ledger ledger) {
        this.ledgers.add(ledger);
        ledger.getTags().add(this);
        this.usageCount++;
    }
    
    public void removeLedger(Ledger ledger) {
        this.ledgers.remove(ledger);
        ledger.getTags().remove(this);
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
} 