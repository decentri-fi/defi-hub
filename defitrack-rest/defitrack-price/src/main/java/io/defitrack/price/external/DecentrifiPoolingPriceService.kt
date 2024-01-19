package io.defitrack.price.external

import io.defitrack.domain.FungibleToken
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiPoolingPriceService(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleToken): Boolean {
        return decentrifiPoolingPriceRepository.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return decentrifiPoolingPriceRepository.cache.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(fungibleToken: FungibleToken): BigDecimal {
        return decentrifiPoolingPriceRepository.getPrice(fungibleToken).also {
            logger.info("getting price on decentrifi pooling for token ${fungibleToken.name} (${fungibleToken.symbol})")
        }
    }
}