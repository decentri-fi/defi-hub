package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.decentrifi.DecentrifiLendingPriceRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiLendingPriceService(
    private val decentrifiLendingPriceRepository: DecentrifiLendingPriceRepository
) : ExternalPriceService {

    override fun appliesTo(tokenInformationVO: TokenInformationVO): Boolean {
        return decentrifiLendingPriceRepository.contains(tokenInformationVO.address.lowercase())
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return decentrifiLendingPriceRepository.getPrice(tokenInformationVO.address.lowercase())
    }
}