package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.decentrifi.DecentriUniswapV2UnderlyingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnBean(DecentriUniswapV2UnderlyingPriceRepository::class)
class DecentrifiUniswapV2PriceService(
    private val repository: DecentriUniswapV2UnderlyingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return repository.contains(token.address)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return repository.prices.asMap().entries.map {
            ExternalPrice(it.key.toString(), Network.ETHEREUM, it.value, "uniswap-v2")
        }
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return repository.getPrice(fungibleToken.address)?.also {
            logger.info("getting logging price on decentrifi uniswapv2 for ${fungibleToken.name} (${fungibleToken.symbol}) on ${fungibleToken.network.name}")
        } ?: BigDecimal.ZERO
    }
}