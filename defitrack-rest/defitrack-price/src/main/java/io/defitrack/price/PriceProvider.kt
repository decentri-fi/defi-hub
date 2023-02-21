package io.defitrack.price

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun getPrice(network: Network, token: TokenInformationVO): BigDecimal {
        return externalPriceServices.find {
            it.appliesTo(network, token)
        }?.getPrice(network, token) ?: beefyPriceService.getPrices()
            .getOrDefault(synonyms.getOrDefault(token.symbol.uppercase(), token.symbol.uppercase()), null)
        ?: withContext(
            Dispatchers.IO
        ) { coinGeckoPriceService.getPrice(token.symbol) } ?: BigDecimal.ZERO
    }
}