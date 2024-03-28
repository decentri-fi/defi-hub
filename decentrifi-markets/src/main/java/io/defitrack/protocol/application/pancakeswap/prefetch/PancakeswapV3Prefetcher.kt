package io.defitrack.protocol.application.pancakeswap.prefetch

import io.defitrack.adapter.output.domain.market.PoolingMarketInformationDTO
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.protocol.Company
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PANCAKESWAP)
//TODO: use correct client
class PancakeswapV3Prefetcher(
    private val httpClient: HttpClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val fetches = lazyAsync {
        httpClient.get("https://api.decentri.fi/pancakeswap/pooling/all-markets")
            .body<List<PoolingMarketInformationDTO>>()
    }

    suspend fun getPrefetches(
        network: Network
    ): List<PoolingMarketInformationDTO> = withContext(Dispatchers.IO) {
        try {
            val prefetches = fetches.await().filter {
                it.network.name == network.name
            }
            logger.info("Prefetched ${prefetches.size} Pancakeswap V3 markets for network $network")
            prefetches
        } catch (ex: Exception) {
            logger.info("Failed to prefetch Uniswap V3 markets", ex)
            emptyList()
        }
    }
}