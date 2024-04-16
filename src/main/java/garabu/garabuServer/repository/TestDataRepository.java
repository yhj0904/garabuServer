package garabu.garabuServer.repository;

import garabu.garabuServer.domain.TestData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestDataRepository extends JpaRepository<TestData, Long> {
}
