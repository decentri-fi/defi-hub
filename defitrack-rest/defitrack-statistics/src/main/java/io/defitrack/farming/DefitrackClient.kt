package io.defitrack.farming

import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.protocol.ProtocolVO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefitrackClient(private val httpClient: HttpClient) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getProtocols(): List<ProtocolVO> {
        return httpClient.get("https://api.defitrack.io/protocols").body()
    }

    suspend fun getFarmingMarkets(protocolVO: ProtocolVO): List<FarmingMarketVO> {
        return try {
            httpClient.get("https://api.defitrack.io/${protocolVO.slug}/farming/all-markets").body()
        } catch (ex: Exception) {
            ex.printStackTrace()
            logger.error("Unable to get farming markets for ${protocolVO.slug}")
            emptyList()
        }
    }
}