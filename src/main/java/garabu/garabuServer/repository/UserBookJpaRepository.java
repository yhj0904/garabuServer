package garabu.garabuServer.repository;


import garabu.garabuServer.domain.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookJpaRepository extends JpaRepository<UserBook, Long> {
}
