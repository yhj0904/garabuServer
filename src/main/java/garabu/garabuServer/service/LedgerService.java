package garabu.garabuServer.service;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.LedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerJpaRepository ledgerJpaRepository;

    public Long registLedger (Ledger ledger){

        ledgerJpaRepository.save(ledger);

        return ledger.getId();
    }

        public List<Ledger> findAllLedgersByMember(Member member) {
        return ledgerJpaRepository.findByMember(member);
    }

        public List<Ledger> findAllLedgers() {
         return ledgerJpaRepository.findAll();
    }
}
