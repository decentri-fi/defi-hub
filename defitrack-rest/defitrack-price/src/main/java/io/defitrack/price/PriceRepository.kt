package io.defitrack.price

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class PriceRepository(
    private val objectMapper: ObjectMapper,
    @Value("\${priceResourceLocation:http://defitrack-price:8080}") val priceResourceLocation: String,
    private val client: HttpClient
) {

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.minutes(10)
    ).build<String, Map<String, BigDecimal>>()

    fun getPrices(): Map<String, BigDecimal> {
        return runBlocking {
            cache.get("prices") {
                val result = client.get<String>(with(HttpRequestBuilder()) {
                    url(priceResourceLocation)
                    this
                })
                objectMapper.readValue(
                    result,
                    object : TypeReference<Map<String, BigDecimal>>() {}).map { entry ->
                    entry.key.uppercase() to entry.value
                }.toMap()
            }
        }
    }
}