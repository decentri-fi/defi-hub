package io.defitrack.price.external.adapter.coingecko

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.nio.charset.Charset

@Service
class CoinGeckoPriceService(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {

    companion object {
        val coinlistLocation = "https://api.coingecko.com/api/v3/coins/list?include_platform=true"
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    val tokenCache = Cache.Builder<String, Set<CoingeckoToken>>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 4) // every 4 hours
    fun init() = runBlocking {
        logger.info("initializing coingecko token cache")
        val tokens = getCoingeckoTokens()
        logger.info("coingecko token cache initialized with ${tokens.size} tokens")
    }

    suspend fun getCoingeckoTokens(): Set<CoingeckoToken> {
        return tokenCache.get("all") {
            val result = withContext(Dispatchers.IO) {
                httpClient.get(coinlistLocation).bodyAsText(Charset.defaultCharset())
            }
            objectMapper.readValue(result, object : TypeReference<Set<CoingeckoToken>>() {})
        }
    }

    suspend fun getTokenByAddress(address: String): CoingeckoToken? {
        return getCoingeckoTokens().firstOrNull { token ->
            token.platforms.entries.any {
                it.value != null && (it.value.lowercase() == address.lowercase())
            }
        }
    }

    suspend fun getPrice(address: String): BigDecimal? = withContext(Dispatchers.IO) {
        try {
            if (address.isBlank()) {
                BigDecimal.ZERO
            } else {
                getTokenByAddress(address)?.let { token ->
                    val response: String =
                        httpClient.get("https://api.coingecko.com/api/v3/simple/price?ids=${token.id}&vs_currencies=usd")
                            .bodyAsText()
                    val jsonObject = JsonParser.parseString(response)
                    jsonObject.asJsonObject[token.id].asJsonObject["usd"].asBigDecimal.also {
                        logger.info("had to get ${token.name} on coingecko")
                    }
                }
            }
        } catch (ex: Exception) {
            logger.debug("error trying to fetch price for $address", ex)
            null
        }
    }
}