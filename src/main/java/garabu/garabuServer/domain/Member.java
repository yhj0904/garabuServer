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
    private Long id;

    private String name;
    @Email
    private String email;
    private String pwd;

    @OneToMany(mappedBy = "member")
    private List<UserBook> userBooks = new ArrayList<>();
}
