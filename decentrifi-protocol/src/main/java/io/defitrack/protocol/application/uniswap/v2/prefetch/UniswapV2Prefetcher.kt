package io.defitrack.protocol.application.uniswap.v2.prefetch

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.protocol.Company
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.UNISWAP)
class UniswapV2Prefetcher(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val fetches: Deferred<List<PoolingMarketInformation>> = lazyAsync {
        val asString: String =
            httpClient.get("https://raw.githubusercontent.com/decentri-fi/data/master/pre-fetches/uniswap/uniswap_v2.json")
                .body()
        objectMapper.readValue(asString, object : TypeReference<List<PoolingMarketInformation>>() {})
    }

    suspend fun getPrefetches(network: Network): List<PoolingMarketInformation> = withContext(Dispatchers.IO) {
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