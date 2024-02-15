package io.defitrack.price.decentrifi

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.external.ExternalPrice
import io.defitrack.price.external.StablecoinPriceProvider
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors

@Component
@ConditionalOnProperty("oracles.velodrome_v2.enabled", havingValue = "true", matchIfMissing = true)
class DecentriVelodromeV2UnderlyingPriceRepository(
    private val markets: Markets,
    private val stablecoinPriceProvider: StablecoinPriceProvider,
) : PriceRepository() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val prices = Cache.Builder<String, ExternalPrice>().build()

    override fun populate() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                val pools = getUniswapV2Pools()

                importNonStableUsdPairs(pools)
                logger.info("Decentri Velodrome V2 Underlying Price Repository populated with ${prices.asMap().entries.size} prices")
            }
        }
    }

    private suspend fun importNonStableUsdPairs(pools: List<PoolingMarketInformation>) {
        val usdPairs = pools.filter { pool ->
            pool.breakdown?.any { share ->
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            } ?: false
        }


        usdPairs.filter {
            it.metadata.getOrDefault("stable", "false") == "false"
        }.forEach { pool ->
            val usdShare = pool.breakdown?.find {
                stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(it.token.address)
            }

            val otherShare = pool.breakdown?.find { share ->
                !stablecoinPriceProvider.isStable(pool.network.toNetwork()).invoke(share.token.address)
            }

            if (usdShare != null && otherShare != null) {
                prices.put(
                    toIndex(usdShare.token.address.lowercase()),
                    ExternalPrice(
                        usdShare.token.address.lowercase(),
                        pool.network.toNetwork(),
                        BigDecimal.ONE,
                        "velodrome-v2",
                        pool.name
                    )
                )

                if (otherShare.reserve > BigInteger.ZERO) {
                    val otherprice = usdShare.reserve.asEth(usdShare.token.decimals).dividePrecisely(
                        otherShare.reserve.asEth(otherShare.token.decimals)
                    )
                    prices.put(
                        toIndex(otherShare.token.address), ExternalPrice(
                            otherShare.token.address,
                            pool.network.toNetwork(),
                            otherprice,
                            "velodrome-v2",
                            pool.name
                        )
                    )
                }
            }
        }
    }

    fun toIndex(address: String): String {
        return address.lowercase()
    }

    fun getPrice(address: String): ExternalPrice? {
        return prices.get(toIndex(address))
    }

    fun contains(address: String): Boolean {
        return prices.asMap().containsKey(toIndex(address))
    }

    suspend fun getUniswapV2Pools(): List<PoolingMarketInformation> {
        return markets.getPoolingMarkets("velodrome_v2")
    }
}