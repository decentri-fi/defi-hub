package io.defitrack.protocol.uniswap.v2.pooling.prefetch

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class UniswapV2Prefetcher(
    private val httpClient: HttpClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val fetches = lazyAsync {
        httpClient.get("https://api.decentri.fi/uniswap_v2/pooling/all-markets")
            .body<List<PoolingMarketVO>>()
    }

    suspend fun getPrefetches(network: Network): List<PoolingMarketVO> = withContext(Dispatchers.IO) {
        try {
            val prefetched = fetches.await().filter {
                it.network.name == network.name
            }
            logger.info("Prefetched ${prefetched.size} Uniswap V2 markets")
            prefetched
        } catch (ex: Exception) {
            logger.info("Failed to prefetch Uniswap V2 markets", ex)
            emptyList()
        }
    }
}