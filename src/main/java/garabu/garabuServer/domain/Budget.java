package garabu.garabuServer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 예산 엔티티
 *
 * 가계부별 월간 예산을 관리하는 JPA 엔티티입니다.
 * 수입과 지출에 대한 예산을 설정할 수 있습니다.
 */
@Entity
@Table(name = "budget",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_book_budget_month",
           columnNames = {"book_id", "budget_month"}
       ),
       indexes = {
           @Index(name = "idx_budget_book_id", columnList = "book_id"),
           @Index(name = "idx_budget_month", columnList = "budget_month"),
           @Index(name = "idx_budget_book_month", columnList = "book_id, budget_month")
       })
@Getter @Setter
@Schema(description = "예산 엔티티")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    @Schema(description = "예산 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_book"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner", "userBooks"})
    @Schema(description = "소속 가계부")
    private Book book;

    @Schema(description = "예산 년월", example = "2025-01")
    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth; // YYYY-MM 형식

    @Schema(description = "수입 예산", example = "5000000")
    @Column(name = "income_budget")
    private Integer incomeBudget;

    @Schema(description = "지출 예산", example = "3000000")
    @Column(name = "expense_budget")
    private Integer expenseBudget;

    @Schema(description = "예산 메모", example = "1월 예산")
    @Column(name = "memo", length = 500)
    private String memo;

    @Schema(description = "생성일")
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Schema(description = "수정일")
    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now();
    }

    /**
     * 예산 정보 업데이트
     */
    public void updateBudget(Integer incomeBudget, Integer expenseBudget, String memo) {
        this.incomeBudget = incomeBudget;
        this.expenseBudget = expenseBudget;
        this.memo = memo;
    }
} 