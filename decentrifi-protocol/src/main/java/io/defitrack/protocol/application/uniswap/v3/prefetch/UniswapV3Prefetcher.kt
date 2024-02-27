package io.defitrack.protocol.uniswap.v3.prefetch

import com.fasterxml.jackson.databind.ObjectMapper
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
@ConditionalOnCompany(Company.UNISWAP)
class UniswapV3Prefetcher(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val fetches = lazyAsync {
        httpClient.get("https://raw.githubusercontent.com/decentri-fi/data/master/pre-fetches/uniswap/uniswap_v3.json")
            .body<String>()
    }

    suspend fun getPrefetches(network: Network): List<PoolingMarketInformation> = withContext(Dispatchers.IO) {
        try {
            val prefetchesAsString = fetches.await()
            val prefetches = objectMapper.readValue(prefetchesAsString, Array<PoolingMarketInformation>::class.java)
                .filter {
                    it.network.name == network.name
                }
            logger.info("Prefetched ${prefetches.size} Uniswap V3 markets for network $network")
            prefetches
        } catch (ex: Exception) {
            logger.info("Failed to prefetch Uniswap V3 markets", ex)
            emptyList()
        }
    }
}