package garabu.garabuServer.mapper;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.dto.LedgerSearchConditionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LedgerMapper {

    long countSearchLedgers(@Param("c") LedgerSearchConditionDTO c);

    List<Ledger> searchLedgers(@Param("c") LedgerSearchConditionDTO c,
                               @Param("orderBy") String orderBy,
                               @Param("offset")  long offset,
                               @Param("limit")   long limit);
}

