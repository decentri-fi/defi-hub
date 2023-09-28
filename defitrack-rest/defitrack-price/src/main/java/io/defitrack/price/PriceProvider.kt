package io.defitrack.price

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.external.ExternalPriceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PriceProvider(
    private val externalPriceServices: List<ExternalPriceService>,
    private val coinGeckoPriceService: CoinGeckoPriceService,
    private val beefyPriceService: BeefyPricesService
) {
    val synonyms = mapOf(
        "WETH" to "ETH",
        "WMATIC" to "MATIC",
        "miMATIC" to "MAI",
        "WBTC" to "BTC"
    )

    suspend fun getPrice(token: TokenInformationVO): BigDecimal? {
        return externalPriceServices.find {
            it.appliesTo(token)
        }?.getPrice(token) ?: beefyPriceService.getPrices()
            .getOrDefault(synonyms.getOrDefault(token.symbol.uppercase(), token.symbol.uppercase()), null)
        ?:  withContext(
            Dispatchers.IO
        ) { coinGeckoPriceService.getPrice(token.address) }
    }
}