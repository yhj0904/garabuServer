package garabu.garabuServer.service;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.repository.LedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerJpaRepository ledgerJpaRepository;

    public Long registLedger (Ledger ledger){
        ledgerJpaRepository.save(ledger);

        return ledger.getId();
    }
}
