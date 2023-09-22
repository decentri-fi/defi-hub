package io.defitrack.protocol.uniswap.v3

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class UniswapV3Prefetcher(
    private val httpClient: HttpClient
) {

    val fetches = lazyAsync {
        httpClient.get("https://api.decentri.fi/uniswap_v3/pooling/all-markets")
            .body<List<PoolingMarketVO>>()
    }

    suspend fun getPrefetches(network: Network): List<PoolingMarketVO> = withContext(Dispatchers.IO) {
        try {
            fetches.await().filter {
                it.network.name == network.name
            }
        } catch (ex: Exception) {
            emptyList()
        }
    }
}