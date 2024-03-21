package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerJpaRepository extends JpaRepository<Ledger, Long> {
}
