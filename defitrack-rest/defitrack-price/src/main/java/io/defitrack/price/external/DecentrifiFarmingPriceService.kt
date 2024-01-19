package io.defitrack.price.external

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.decentrifi.DecentrifiFarmingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiFarmingPriceService(
    private val priceRepo: DecentrifiFarmingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return priceRepo.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return priceRepo.cache.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return priceRepo.getPrice(fungibleToken).also {
            logger.info("getting price on decentrifi farming for token ${fungibleToken.name} (${fungibleToken.symbol})")
        }
    }
}