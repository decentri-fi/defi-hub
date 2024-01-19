package io.defitrack.price.decentrifi

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.pooling.PoolingMarketInformation
import io.defitrack.market.domain.pooling.PoolingMarketTokenShareInformation
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.port.`in`.PricePort
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors

@Component
class DecentriUniswapV2UnderlyingPriceRepository(
    private val markets: Markets
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val prices = Cache.Builder<String, BigDecimal>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 1) // every 24 hours
    fun populatePrices() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                val pools = getUniswapV2Pools()

                importUsdPairs(pools)
                logger.info("Decentri Uniswap V2 Underlying Price Repository populated with ${prices.asMap().entries.size} prices")
            }
        }
    }

    private fun importUsdPairs(pools: List<PoolingMarketInformation>) {
        val stableCoinPredicate: (PoolingMarketTokenShareInformation) -> Boolean = { share ->
            share.token.address.lowercase() == "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
                    || share.token.address.lowercase() == "0xdac17f958d2ee523a2206206994597c13d831ec7"
                    || share.token.address.lowercase() == "0x6b175474e89094c44da98b954eedeac495271d0f"
        }

        val usdPairs = pools.filter {
            it.breakdown?.any(stableCoinPredicate) ?: false
        }

        usdPairs.forEach { pool ->
            val usdShare = pool.breakdown?.find(stableCoinPredicate)

            val otherShare = pool.breakdown?.find { share ->
                share.token.address.lowercase() != "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
                        && share.token.address.lowercase() != "0xdac17f958d2ee523a2206206994597c13d831ec7"
                        && share.token.address.lowercase() != "0x6b175474e89094c44da98b954eedeac495271d0f"
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

    suspend fun getUniswapV2Pools(): List<PoolingMarketInformation> {
        return markets.getPoolingMarkets("uniswap_v2")
    }
}