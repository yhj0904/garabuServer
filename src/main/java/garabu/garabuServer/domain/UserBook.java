package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 가계부-사용자 매핑 엔티티
 *
 * 가계부와 사용자(회원) 간의 관계 및 역할을 관리하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "가계부-사용자 매핑 엔티티")
public class UserBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "공동작업 ID")
    private Long id;    //공동작업 ID

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    @Schema(description = "작업자 회원")
    private Member member;  //  작업자 ID

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "book_id")
    @Schema(description = "가계부")
    private Book book;      // 가게부 ID

    @Schema(description = "가계부 내 역할", example = "OWNER")
    private UserRole userRole; // 예를 들어, "OWNER", "MEMBER" 등의 역할 구분
}
