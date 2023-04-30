package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiPoolingPriceService(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository
) : ExternalPriceService {

    override fun appliesTo(tokenInformationVO: TokenInformationVO): Boolean {
        return decentrifiPoolingPriceRepository.contains(tokenInformationVO.address.lowercase())
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return decentrifiPoolingPriceRepository.getPrice(tokenInformationVO.address)
    }
}