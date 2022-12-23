package io.defitrack.price

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

@Component
class PriceResource(
    @Value("\${priceResourceLocation:http://defitrack-price:8080}") val priceResourceLocation: String,
    private val client: HttpClient
) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    val cache = Cache.Builder()
        .expireAfterWrite(10.minutes)
        .build<String, BigDecimal>()

    suspend fun getPrice(symbol: String): BigDecimal {
        return cache.get(symbol) {
            try {
                client.get("$priceResourceLocation/$symbol").body()
            } catch (ex: Exception) {
                logger.error("unable to fetch price for $symbol", ex)
                BigDecimal.ZERO
            }
        }
    }

    suspend fun calculatePrice(name: String, amount: Double): Double {
        val price = getPrice(name)
        return amount.times(price.toDouble())
    }

    suspend fun calculatePrice(priceRequest: PriceRequest?): Double {
        return priceRequest?.let {
            try {
                client.post(priceResourceLocation) {
                    this.header("Content-Type", "application/json")
                    setBody(priceRequest)
                }.body()
            } catch (ex: Exception) {
                logger.error("unable to fetch price for ${it.address}")
                ex.printStackTrace()
                0.0
            }
        } ?: 0.0
    }
}