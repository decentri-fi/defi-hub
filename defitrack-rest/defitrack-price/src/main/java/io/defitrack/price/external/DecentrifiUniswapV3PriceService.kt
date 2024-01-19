package io.defitrack.price.external

import io.defitrack.domain.FungibleToken
import io.defitrack.price.decentrifi.DecentrifiUniswapV3UnderlyingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiUniswapV3PriceService(
    private val repository: DecentrifiUniswapV3UnderlyingPriceRepository
) : ExternalPriceService {

    override fun order(): Int = 2

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleToken): Boolean {
        return repository.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return repository.prices.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(fungibleToken: FungibleToken): BigDecimal {
        return repository.getPrice(fungibleToken)?.also {
            logger.info("getting price on decentrifi uniswapv3 for ${fungibleToken.name} (${fungibleToken.symbol}) on ${fungibleToken.network.name}")
        } ?: BigDecimal.ZERO
    }
}