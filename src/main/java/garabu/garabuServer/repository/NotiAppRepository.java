package garabu.garabuServer.repository;

import garabu.garabuServer.domain.NotiApp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotiAppRepository extends JpaRepository<NotiApp, String> {
}
