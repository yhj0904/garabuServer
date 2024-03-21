package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class UserBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    //공동작업 ID

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  //  작업자 ID

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "book_id")
    private Book book;      // 가게부 ID

    private UserRole userRole; // 예를 들어, "OWNER", "MEMBER" 등의 역할 구분
}
