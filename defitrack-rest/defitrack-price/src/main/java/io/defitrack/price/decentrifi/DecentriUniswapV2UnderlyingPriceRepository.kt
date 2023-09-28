package io.defitrack.price.decentrifi

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.vo.PoolingMarketTokenShareVO
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.network.NetworkVO
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors

@Component
class DecentriUniswapV2UnderlyingPriceRepository(
    private val httpClient: HttpClient
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

    private fun importUsdPairs(pools: List<PoolingMarketVO>) {
        val stableCoinPredicate: (PoolingMarketTokenShareVO) -> Boolean = { share ->
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

    suspend fun getUniswapV2Pools(): List<PoolingMarketVO> = withContext(Dispatchers.IO) {
        val result = httpClient.get("https://api.decentri.fi/uniswap_v2/pooling/all-markets")
        if (result.status.isSuccess()) result.body()
        else {
            logger.error("Unable to fetch pools for UNISWAP_V2, result was ${result.body<String>()}")
            emptyList()
        }
    }
}