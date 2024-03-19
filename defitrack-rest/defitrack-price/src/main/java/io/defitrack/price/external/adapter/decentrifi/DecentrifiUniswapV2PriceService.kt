package io.defitrack.price.external.adapter.decentrifi

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.external.adapter.stable.StablecoinPriceProvider
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
@ConditionalOnProperty("oracles.uniswap_v2.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiUniswapV2PriceService(
    private val markets: Markets,
    private val stablecoinPriceProvider: StablecoinPriceProvider,
) : ExternalPriceService {

    override fun order(): Int = 50

    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        getPrices().forEach {
            send(it)
        }
    }

    private val prices: MutableList<ExternalPrice> = mutableListOf()

    fun getPrices(): List<ExternalPrice> = runBlocking {
        val pools = getUniswapV2Pools()
        importUsdPairs(pools)
        prices
    }

    private suspend fun importUsdPairs(pools: List<PoolingMarketInformation>) {
        val usdPairs = pools.filter { pool ->
            pool.breakdown?.any { share ->
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            } ?: false
        }


        usdPairs.forEach { pool ->
            val usdShare = pool.breakdown?.find {
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(it.token.address)
            }

            val otherShare = pool.breakdown?.find { share ->
                !stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            }

            if (usdShare != null && otherShare != null) {

                prices.add(
                    ExternalPrice(
                        usdShare.token.address.lowercase(),
                        pool.network.toNetwork(),
                        BigDecimal.valueOf(1.0),
                        "uniswap_v2",
                        pool.name,
                        order()
                    )
                )

                if (otherShare.reserve > BigInteger.ZERO) {
                    val otherprice = usdShare.reserve.asEth(usdShare.token.decimals).dividePrecisely(
                        otherShare.reserve.asEth(otherShare.token.decimals)
                    )

                    prices.add(
                        ExternalPrice(
                            otherShare.token.address,
                            pool.network.toNetwork(),
                            otherprice,
                            "uniswap_v2",
                            pool.name,
                            order()
                        )
                    )
                }
            }
        }
    }

    suspend fun getUniswapV2Pools(): List<PoolingMarketInformation> {
        return markets.getPoolingMarkets("uniswap_v2")
    }


}