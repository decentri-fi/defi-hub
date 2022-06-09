package io.defitrack.price

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PriceProvider(
    private val externalPriceServices: List<ExternalPriceService>,
    private val beefyPriceService: BeefyPricesService,
    private val coinGeckoPriceService: CoinGeckoPriceService
) {
    val synonyms = mapOf(
        "WETH" to "ETH",
        "WMATIC" to "MATIC",
        "miMATIC" to "MAI",
        "WBTC" to "BTC"
    )

    fun getPrice(symbol: String): BigDecimal {
        return externalPriceServices.find {
            it.appliesTo(symbol)
        }?.getPrice() ?: beefyPriceService.getPrices()
            .getOrDefault(synonyms.getOrDefault(symbol.uppercase(), symbol.uppercase()), null) ?: runBlocking(
            Dispatchers.IO
        ) { coinGeckoPriceService.getPrice(symbol) } ?: BigDecimal.ZERO
    }
}