package io.defitrack.statistics.client

import com.google.gson.JsonParser
import io.defitrack.protocol.ProtocolVO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefitrackClient(
    private val httpClient: HttpClient,
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getProtocols(): List<ProtocolVO> = withContext(Dispatchers.IO) {
        httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getFarmingMarketsCount(protocolVO: ProtocolVO): Int = withContext(Dispatchers.IO) {
        try {
            JsonParser.parseString(httpClient.get("https://api.decentri.fi/${protocolVO.slug}/farming/markets?paged&size=1") {
                timeout {
                    this.requestTimeoutMillis = 3000
                }
            }.bodyAsText()).asJsonObject["totalElements"].asInt
        } catch (ex: Exception) {
            logger.error("Unable to get farming markets for ${protocolVO.slug}")
            0
        }
    }

    suspend fun getPoolingMarketsCount(protocolVO: ProtocolVO): Int = withContext(Dispatchers.IO) {
        try {
            JsonParser.parseString(httpClient.get("https://api.decentri.fi/${protocolVO.slug}/pooling/markets?paged&size=1") {
                timeout {
                    this.requestTimeoutMillis = 3000
                }
            }.bodyAsText()).asJsonObject["totalElements"].asInt
        } catch (ex: Exception) {
            logger.error("Unable to get pooling markets for ${protocolVO.slug}")
            0
        }
    }

    suspend fun getLendingMarketCount(protocolVO: ProtocolVO): Int = withContext(Dispatchers.IO) {
        try {
            JsonParser.parseString(httpClient.get("https://api.decentri.fi/${protocolVO.slug}/lending/markets?paged&size+1") {
                timeout {
                    this.requestTimeoutMillis = 3000
                }
            }.bodyAsText()).asJsonObject["totalElements"].asInt
        } catch (ex: Exception) {
            logger.error("Unable to get lending markets for ${protocolVO.slug}")
            0
        }
    }
}