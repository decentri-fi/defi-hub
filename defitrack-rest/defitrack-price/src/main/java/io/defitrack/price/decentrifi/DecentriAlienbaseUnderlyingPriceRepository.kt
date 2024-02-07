package io.defitrack.price.decentrifi

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.external.StablecoinPriceProvider
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors

@Component
class DecentriAlienbaseUnderlyingPriceRepository(
    private val markets: Markets,
    private val stablecoinPriceProvider: StablecoinPriceProvider
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val prices = Cache.Builder<String, BigDecimal>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 1)
    fun populatePrices() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                val pools = getAlienbasePools()

                importUsdPairs(pools)
                logger.info("Decentri Alienbase V2 Underlying Price Repository populated with ${prices.asMap().entries.size} prices")
            }
        }
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
                prices.put(toIndex(usdShare.token.address.lowercase()), BigDecimal.valueOf(1.0))

                if (otherShare.reserve > BigInteger.ZERO) {
                    val otherprice = usdShare.reserve.asEth(usdShare.token.decimals).dividePrecisely(
                        otherShare.reserve.asEth(otherShare.token.decimals)
                    )
                    prices.put(toIndex(otherShare.token.address), otherprice)
                }
            }
        }
    }


    fun toIndex(address: String): String {
        return address.lowercase()
    }

    fun getPrice(address: String): BigDecimal? {
        return prices.get(toIndex(address))
    }

    fun contains(address: String): Boolean {
        return prices.asMap().containsKey(toIndex(address))
    }

    suspend fun getAlienbasePools(): List<PoolingMarketInformation> {
        return markets.getPoolingMarkets("alienbase")
    }
}