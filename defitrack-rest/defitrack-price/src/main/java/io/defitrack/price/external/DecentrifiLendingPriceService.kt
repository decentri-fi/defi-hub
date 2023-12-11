package io.defitrack.price.external

import io.defitrack.erc20.FungibleToken
import io.defitrack.price.decentrifi.DecentrifiLendingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiLendingPriceService(
    private val decentrifiLendingPriceRepository: DecentrifiLendingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleToken): Boolean {
        return decentrifiLendingPriceRepository.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return decentrifiLendingPriceRepository.cache.asMap().entries.map(Map.Entry<Any?, ExternalPrice>::value)
    }

    override suspend fun getPrice(fungibleToken: FungibleToken): BigDecimal {
        return decentrifiLendingPriceRepository.getPrice(fungibleToken).also {
            logger.info("got price on decentrifi lending")
        }
    }
}