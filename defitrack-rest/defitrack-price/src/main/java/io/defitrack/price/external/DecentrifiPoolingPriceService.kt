package io.defitrack.price.external

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.decentrifi.DecentrifiLendingPriceRepository
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnBean(DecentrifiPoolingPriceRepository::class)
class DecentrifiPoolingPriceService(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return decentrifiPoolingPriceRepository.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return decentrifiPoolingPriceRepository.cache.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return decentrifiPoolingPriceRepository.getPrice(fungibleToken).also {
            logger.debug("getting price on decentrifi pooling for token ${fungibleToken.name} (${fungibleToken.symbol})")
        }
    }
}