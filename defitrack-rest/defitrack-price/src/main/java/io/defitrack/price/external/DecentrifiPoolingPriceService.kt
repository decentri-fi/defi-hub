package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiPoolingPriceService(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository
) : ExternalPriceService {

    override fun appliesTo(token: TokenInformationVO): Boolean {
        return decentrifiPoolingPriceRepository.contains(token)
    }

    override fun getAllPrices(): List<ExternalPrice> {
        return decentrifiPoolingPriceRepository.cache.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(token: TokenInformationVO): BigDecimal {
        return decentrifiPoolingPriceRepository.getPrice(token)
    }
}