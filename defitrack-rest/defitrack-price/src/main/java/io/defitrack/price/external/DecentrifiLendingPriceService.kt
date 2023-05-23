package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.decentrifi.DecentrifiLendingPriceRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiLendingPriceService(
    private val decentrifiLendingPriceRepository: DecentrifiLendingPriceRepository
) : ExternalPriceService {

    override fun appliesTo(token: TokenInformationVO): Boolean {
        return decentrifiLendingPriceRepository.contains(token)
    }

    override suspend fun getPrice(token: TokenInformationVO): BigDecimal {
        return decentrifiLendingPriceRepository.getPrice(token)
    }
}