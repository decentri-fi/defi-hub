package io.defitrack.protocol.beefy.apy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

@Service
class BeefyAPYService(
    private val beefyAPIEndpoint: String = "https://api.beefy.finance",
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val cache = Cache.Builder()
        .expireAfterWrite(1.hours)
        .build<String, Map<String, BigDecimal>>()

    fun getAPYS(): Map<String, BigDecimal> {
        return runBlocking {
            cache.get("beefy-api-apys") {
                val result: String = client.get(with(HttpRequestBuilder()) {
                    url("$beefyAPIEndpoint/apy")
                    this
                }).body()
                objectMapper.readValue(
                    result,
                    object : TypeReference<Map<String, BigDecimal>>() {})
            }
        }
    }
}
