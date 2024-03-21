package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;                // 가계부 식별자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;      // 소유한 사용자의 식별자 Id

    private String BookName;   // 가계부 이름 ex)개인 가계부, 커플 가계부

    @OneToMany(mappedBy = "book")
    private List<UserBook> userBooks = new ArrayList<>();

}
