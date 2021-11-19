package io.defitrack.price

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class PriceResource(
    @Value("\${priceResourceLocation:http://defitrack-price:8080}") val priceResourceLocation: String,
    private val client: HttpClient
) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder()
        .expireAfterWrite(Duration.Companion.minutes(10))
        .build<String, BigDecimal>()

    fun getPrice(tokenName: String): BigDecimal {
        return runBlocking {
            cache.get(tokenName) {
                try {
                    client.get("$priceResourceLocation/$tokenName")
                } catch (ex: Exception) {
                    logger.error("unable to fetch price for $tokenName", ex)
                    BigDecimal.ZERO
                }
            }
        }
    }

    fun calculatePrice(name: String, amount: Double): Double {
        val price = getPrice(name)
        return amount.times(price.toDouble())
    }

    fun calculatePrice(priceRequest: PriceRequest?): Double = runBlocking {
        priceRequest?.let {
            try {
                client.post(priceResourceLocation) {
                    this.header("Content-Type", "application/json")
                    this.body = priceRequest
                }
            } catch (ex: Exception) {
                logger.error("unable to fetch price for ${it.address}")
                0.0
            }
        } ?: 0.0
    }
}