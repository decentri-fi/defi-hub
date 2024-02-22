package io.defitrack.price.application

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.external.adapter.coingecko.CoinGeckoPriceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class AggregatedPriceProvider(
    private val priceAggregator: PriceAggregator,
    private val coingeckoSerivce: CoinGeckoPriceService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    suspend fun getPrice(token: FungibleTokenInformation): BigDecimal {
        return priceAggregator.getPrice(token.address, token.network.name.lowercase())?.price
            ?: fromCoingecko(token)
            ?: BigDecimal.ZERO
    }

    private suspend fun fromCoingecko(token: FungibleTokenInformation): BigDecimal? {
        return coingeckoSerivce.getPrice(token.address)?.also {
            logger.debug("getting price on coingecko for ${token.name} (${token.symbol}) on ${token.network.name}")
        } ?: BigDecimal.ZERO
    }
}