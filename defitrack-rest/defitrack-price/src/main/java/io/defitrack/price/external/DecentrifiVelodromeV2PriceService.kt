package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.decentrifi.DecentriUniswapV2UnderlyingPriceRepository
import io.defitrack.price.decentrifi.DecentriVelodromeV2UnderlyingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnBean(DecentriVelodromeV2UnderlyingPriceRepository::class)
class DecentrifiVelodromeV2PriceService(
    private val repository: DecentriVelodromeV2UnderlyingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return repository.contains(token.address)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return repository.prices.asMap().values.toList()
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return repository.getPrice(fungibleToken.address)?.price?.also {
            logger.debug("getting logging price on decentrifi velodromev2 for ${fungibleToken.name} (${fungibleToken.symbol}) on ${fungibleToken.network.name}")
        } ?: BigDecimal.ZERO
    }
}