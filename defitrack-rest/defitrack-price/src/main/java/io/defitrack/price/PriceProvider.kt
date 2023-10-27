package io.defitrack.price

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.external.ExternalPriceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PriceProvider(
    private val externalPriceServices: List<ExternalPriceService>,
    private val beefyPriceService: BeefyPricesService
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val synonyms = mapOf(
        "WETH" to "ETH",
        "WMATIC" to "MATIC",
        "miMATIC" to "MAI",
        "WBTC" to "BTC"
    )

    suspend fun getPrice(token: TokenInformationVO): BigDecimal? {
        return externalPriceServices.find {
            it.appliesTo(token)
        }?.getPrice(token) ?: fromBeefy(token)
    }

    private suspend fun fromBeefy(token: TokenInformationVO): BigDecimal? {
        return beefyPriceService.getPrices()[synonyms.getOrDefault(
            token.symbol.uppercase(),
            token.symbol.uppercase()
        )]?.also {
            logger.info("getting price on beefy")
        }
    }
}