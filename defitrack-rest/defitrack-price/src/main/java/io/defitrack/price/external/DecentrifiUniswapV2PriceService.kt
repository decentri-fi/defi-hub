package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.decentrifi.DecentriUniswapV2UnderlyingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiUniswapV2PriceService(
    private val decentriUniswapV2UnderlyingPriceRepository: DecentriUniswapV2UnderlyingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return decentriUniswapV2UnderlyingPriceRepository.contains(token.address)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return decentriUniswapV2UnderlyingPriceRepository.prices.asMap().entries.map {
            ExternalPrice(it.key.toString(), Network.ETHEREUM, it.value, "uniswap-v2")
        }
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return decentriUniswapV2UnderlyingPriceRepository.getPrice(fungibleToken.address)?.also {
            logger.info("getting logging price on decentrifi uniswapv2 for ${fungibleToken.name} (${fungibleToken.symbol}) on ${fungibleToken.network.name}")
        } ?: BigDecimal.ZERO
    }
}