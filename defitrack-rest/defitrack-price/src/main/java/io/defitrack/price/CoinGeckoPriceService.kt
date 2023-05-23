package io.defitrack.price

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.price.coingecko.CoingeckoToken
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.nio.charset.Charset
import jakarta.annotation.PostConstruct
import kotlin.time.Duration.Companion.days

@Service
class CoinGeckoPriceService(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {

    companion object {
        val coinlistLocation = "https://raw.githubusercontent.com/defitrack/data/master/coingecko/coins.json"
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    val tokenCache = Cache.Builder<String, Set<CoingeckoToken>>().expireAfterWrite(7.days).build()

    @PostConstruct
    fun init() = runBlocking {
        getCoingeckoTokens()
    }

    suspend fun getCoingeckoTokens(): Set<CoingeckoToken> {
        return tokenCache.get("all") {
            val result = withContext(Dispatchers.IO) {
                httpClient.get(coinlistLocation).bodyAsText(Charset.defaultCharset())
            }
            objectMapper.readValue(result, object : TypeReference<Set<CoingeckoToken>>() {})
        }
    }

    suspend fun getTokenBySymbol(symbol: String): CoingeckoToken? {
        return getCoingeckoTokens().firstOrNull { token ->
            token.symbol.uppercase() == symbol.uppercase()
        }
    }

    suspend fun getPrice(symbol: String): BigDecimal? {
        return try {
            if (symbol.isBlank()) {
                BigDecimal.ZERO
            } else {
                getTokenBySymbol(symbol)?.let { token ->
                    val response: String =
                        httpClient.get("https://api.coingecko.com/api/v3/simple/price?ids=${token.id}&vs_currencies=usd")
                            .bodyAsText()
                    val jsonObject = JsonParser.parseString(response)
                    jsonObject.asJsonObject[token.id].asJsonObject["usd"].asBigDecimal
                }
            }
        } catch (ex: Exception) {
            logger.error("error trying to fetch price for $symbol")
            null
        }
    }
}