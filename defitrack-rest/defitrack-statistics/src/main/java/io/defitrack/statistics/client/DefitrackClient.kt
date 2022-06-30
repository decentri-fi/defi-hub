package io.defitrack.statistics.client

import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.market.lending.vo.LendingMarketVO
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.protocol.ProtocolVO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
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
            httpClient.get("https://api.defitrack.io/${protocolVO.slug}/farming/all-markets") {
                timeout {
                    this.requestTimeoutMillis = 3000
                }
            }.body()
        } catch (ex: Exception) {
            logger.error("Unable to get farming markets for ${protocolVO.slug}")
            emptyList()
        }
    }

    suspend fun getPoolingMarkets(protocolVO: ProtocolVO): List<PoolingMarketVO> {
        return try {
            httpClient.get("https://api.defitrack.io/${protocolVO.slug}/pooling/all-markets").body()
        } catch (ex: Exception) {
            logger.error("Unable to get pooling markets for ${protocolVO.slug}")
            emptyList()
        }
    }

    suspend fun getLendingMarkets(protocolVO: ProtocolVO): List<LendingMarketVO> {
        return try {
            httpClient.get("https://api.defitrack.io/${protocolVO.slug}/lending/all-markets").body()
        } catch (ex: Exception) {
            logger.error("Unable to get lending markets for ${protocolVO.slug}")
            emptyList()
        }
    }
}