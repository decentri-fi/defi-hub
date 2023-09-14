package io.defitrack.price.decentrifi

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.network.NetworkVO
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.Executors

@Component
class DecentriUniswapV2UnderlyingPriceRepository(
    private val httpClient: HttpClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    val prices = Cache.Builder<String, List<BigDecimal>>().build()

    @PostConstruct
    fun populatePrices() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                val pools = getUniswapV2Pools()

                val usdPairs = pools.filter {
                    it.breakdown?.any { share ->
                        share.token.name == "USDC" || share.token.name == "USDT" || share.token.name == "DAI"
                    } ?: false
                }

                usdPairs.forEach { pool ->
                    val usdShare = pool.breakdown?.find { share ->
                        share.token.name == "USDC" || share.token.name == "USDT" || share.token.name == "DAI"
                    }

                    val otherShare = pool.breakdown?.find { share ->
                        share.token.name != "USDC" && share.token.name != "USDT" && share.token.name != "DAI"
                    }

                    if (usdShare != null && otherShare != null) {
                        prices.put(toIndex(pool.network, usdShare.token.address), listOf(BigDecimal.valueOf(1.0)))

                        if (prices.get(otherShare.token.address) == null) prices.put(
                            toIndex(pool.network, otherShare.token.address), listOf(
                                otherShare.reserve.asEth(otherShare.token.decimals).dividePrecisely(
                                    usdShare.reserve.asEth(usdShare.token.decimals)
                                )
                            )
                        )
                    }
                }
                logger.info("Decentri Uniswap V2 Underlying Price Repository populated with ${prices.asMap().entries.size} prices")
            }
        }
    }

    fun toIndex(network: NetworkVO, address: String): String {
        return "${network.name}-${address.lowercase()}"
    }

    suspend fun getUniswapV2Pools(): List<PoolingMarketVO> = withContext(Dispatchers.IO) {
        val result = httpClient.get("https://api.decentri.fi/uniswap/pooling/all-markets?protocol=UNISWAP_V2")
        if (result.status.isSuccess()) result.body()
        else {
            logger.error("Unable to fetch pools for UNISWAP_V2, result was ${result.body<String>()}")
            emptyList()
        }
    }
}