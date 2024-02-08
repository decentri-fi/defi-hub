package io.defitrack.price.external

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.decentrifi.DecentrifiLendingPriceRepository
import io.defitrack.price.decentrifi.DecentrifiPancakeswapV3UnderlyingPriceRepository
import io.defitrack.price.decentrifi.DecentrifiUniswapV3UnderlyingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnBean(DecentrifiPancakeswapV3UnderlyingPriceRepository::class)
class DecentrifiPancakeswapV3PriceService(
    private val repository: DecentrifiPancakeswapV3UnderlyingPriceRepository
) : ExternalPriceService {

    override fun order(): Int = 2

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return repository.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return repository.prices.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return repository.getPrice(fungibleToken)?.also {
            logger.info("getting logging price on decentrifi uniswapv3 for ${fungibleToken.name} (${fungibleToken.symbol}) on ${fungibleToken.network.name}")
        } ?: BigDecimal.ZERO
    }
}