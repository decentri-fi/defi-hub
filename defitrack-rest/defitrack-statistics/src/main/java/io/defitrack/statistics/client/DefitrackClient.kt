package io.defitrack.statistics.client

import com.google.gson.JsonParser
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
@Deprecated("Use decentrificlient instead")
class DefitrackClient(
    private val httpClient: HttpClient,
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getFarmingMarketsCount(protocol: String): Int = withContext(Dispatchers.IO) {
        try {
            JsonParser.parseString(httpClient.get("https://api.decentri.fi/${protocol}/farming/markets?paged&size=1") {
                timeout {
                    this.requestTimeoutMillis = 3000
                }
            }.bodyAsText()).asJsonObject["totalElements"].asInt
        } catch (ex: Exception) {
            logger.error("Unable to get farming markets for ${protocol}")
            0
        }
    }

    suspend fun getPoolingMarketsCount(protocol: String): Int = withContext(Dispatchers.IO) {
        try {
            JsonParser.parseString(httpClient.get("https://api.decentri.fi/${protocol}/pooling/markets?paged&size=1") {
                timeout {
                    this.requestTimeoutMillis = 3000
                }
            }.bodyAsText()).asJsonObject["totalElements"].asInt
        } catch (ex: Exception) {
            logger.error("Unable to get pooling markets for ${protocol}")
            0
        }
    }

    suspend fun getLendingMarketCount(protocol: String): Int = withContext(Dispatchers.IO) {
        try {
            JsonParser.parseString(httpClient.get("https://api.decentri.fi/${protocol}/lending/markets?paged&size+1") {
                timeout {
                    this.requestTimeoutMillis = 3000
                }
            }.bodyAsText()).asJsonObject["totalElements"].asInt
        } catch (ex: Exception) {
            logger.error("Unable to get lending markets for ${protocol}")
            0
        }
    }
}