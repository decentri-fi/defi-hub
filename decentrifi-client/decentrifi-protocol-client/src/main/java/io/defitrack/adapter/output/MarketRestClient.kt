package io.defitrack.adapter.output

import io.defitrack.adapter.output.domain.market.FarmingMarketInformationDTO
import io.defitrack.adapter.output.domain.market.LendingMarketInformationDTO
import io.defitrack.adapter.output.domain.market.PoolingMarketInformationDTO
import io.defitrack.port.output.MarketClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
internal class MarketRestClient(
    private val httpClient: HttpClient
) : MarketClient {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getPoolingMarkets(protocolSlug: String): List<PoolingMarketInformationDTO> {
        return withContext(Dispatchers.IO) {
            val result = httpClient.get("https://api.decentri.fi/$protocolSlug/pooling/all-markets")
            if (result.status.isSuccess()) result.body()
            else {
                logger.error("Unable to fetch pools for UNISWAP_V2, result was ${result.body<String>()}")
                emptyList()
            }
        }
    }

    override suspend fun getFarmingMarkets(protocolSlug: String): List<FarmingMarketInformationDTO> {
        val result = httpClient.get("https://api.decentri.fi/${protocolSlug}/farming/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error("Unable to fetch farms for ${protocolSlug} ${result.bodyAsText()}")
            emptyList()
        }
    }

    override suspend fun getLendingMarkets(protocol: String): List<LendingMarketInformationDTO> {
        val result = httpClient.get("https://api.decentri.fi/$protocol/lending/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error(
                "Unable to fetch lending markets for $protocol (${result.bodyAsText()}"
            )
            emptyList()
        }
    }
}