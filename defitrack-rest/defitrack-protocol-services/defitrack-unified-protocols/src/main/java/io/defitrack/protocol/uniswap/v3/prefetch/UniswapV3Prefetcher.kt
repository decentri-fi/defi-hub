package io.defitrack.protocol.uniswap.v3.prefetch

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.protocol.Company
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.UNISWAP)
class UniswapV3Prefetcher(
    private val httpClient: HttpClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val fetches = lazyAsync {
        httpClient.get("https://api.decentri.fi/uniswap_v3/pooling/all-markets")
            .body<List<PoolingMarketVO>>()
    }

    suspend fun getPrefetches(network: Network): List<PoolingMarketVO> = withContext(Dispatchers.IO) {
        try {
            val prefetches = fetches.await().filter {
                it.network.name == network.name
            }
            logger.info("Prefetched ${prefetches.size} Uniswap V3 markets")
            prefetches
        } catch (ex: Exception) {
            logger.info("Failed to prefetch Uniswap V3 markets", ex)
            emptyList()
        }
    }
}