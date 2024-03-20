package io.defitrack.price.external.adapter.coingecko

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.getOrElse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.nio.charset.Charset

@Service
@ConditionalOnProperty("oracles.coingecko.enabled", havingValue = "true", matchIfMissing = true)
class CoinGeckoPriceService(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper,
    @Value("\${coingecko.api-key:test}") private val apiKey: String
) : ExternalPriceService {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val coinlistLocation = "https://api.coingecko.com/api/v3/coins/list?include_platform=true"

    val tokenCache = Cache.Builder<String, Set<CoingeckoToken>>().build()

    @PostConstruct// every 4 hours
    fun init() = runBlocking {
        val tokens = getCoingeckoTokens()
        logger.info("coingecko token cache initialized with ${tokens.size} tokens")
        if (tokens.isEmpty()) {
            throw IllegalStateException("coingecko token cache is empty, we should try again")
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 4, initialDelay = 1000 * 60 * 60 * 4)
    fun scheduledRefresh() = runBlocking {
        getCoingeckoTokens()
    }

    suspend fun getCoingeckoTokens(): Set<CoingeckoToken> {
        return tokenCache.get("all") {
            logger.info("fetching all coingecko tokens")
            catch {
                val result = withContext(Dispatchers.IO) {
                    httpClient.get(coinlistLocation).bodyAsText(Charset.defaultCharset())
                }
                objectMapper.readValue(result, object : TypeReference<Set<CoingeckoToken>>() {})
            }.mapLeft {
                logger.info("error fetching coingecko tokens: {}", it.message)
            }.getOrElse { emptySet() }
        }
    }

    private fun fromNetwork(network: String): Network? {
        return Network.fromString(network)
    }

    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        val tokens = getCoingeckoTokens()
        tokens.chunked(400).map { chunk ->

            val ids = chunk.joinToString(",") { it.id }

            val response = getPrices(ids).bodyAsText()
            val jsonObject = JsonParser.parseString(response)
            chunk.map { token ->
                if (!jsonObject.asJsonObject.has(token.id) || !jsonObject.asJsonObject[token.id].asJsonObject.has("usd")) {
                    logger.debug("price for ${token.id} not found: {}", jsonObject.asJsonObject[token.id].toString())
                    emptyList()
                } else {
                    val price = jsonObject.asJsonObject[token.id].asJsonObject["usd"].asBigDecimal
                    token.platforms.entries.mapNotNull { (network, address) ->
                        fromNetwork(network)?.let {
                            ExternalPrice(address, it, price, "coingecko", "coingecko", 100)
                        }
                    }
                }
            }.flatten()
        }.flatten().forEach {
            send(it)
        }
    }

    suspend fun getPrices(ids: String): HttpResponse {
        delay(1000)
        return retry(limitAttempts(10) + binaryExponentialBackoff(1000, 10000)) {
            try {
                val response =
                    httpClient.get("https://api.coingecko.com/api/v3/simple/price?ids=${ids}&vs_currencies=usd&x_cg_demo_api_key=${apiKey}") { }
                if (response.status.value == 429) {
                    logger.info("problem fetching prices, retrying: {}", response.bodyAsText())
                    throw ThrottledException()
                }
                response
            } catch (ex: Exception) {
                logger.debug("problem trying to get prices from coingecko", ex)
                throw ex
            }
        }
    }

    class ThrottledException : RuntimeException()
}