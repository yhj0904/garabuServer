package garabu.garabuServer.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;            // 회원 식별자 Id

    private String username;        // 닉네임
    @Email
    private String email;       // 이메일
    private String password;         // 비번

    @OneToMany(mappedBy = "member")
    private List<UserBook> userBooks = new ArrayList<>();
}
