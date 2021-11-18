package io.defitrack.price

import io.defitrack.price.PriceRequest
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
class PriceResource(
    @Value("\${priceResourceLocation:http://defitrack-price:8080}") val priceResourceLocation: String,
    private val client: HttpClient
) {

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder()
        .expireAfterWrite(Duration.Companion.minutes(10))
        .build<String, BigDecimal>()

    fun getPrice(tokenName: String): BigDecimal {
        return runBlocking {
            cache.get(tokenName) {
                client.get("$priceResourceLocation/$tokenName")
            }
        }
    }


    fun calculatePrice(name: String, amount: Double): Double {
        val price = getPrice(name)
        return amount.times(price.toDouble())
    }

    fun calculatePrice(priceRequest: PriceRequest?): Double = runBlocking {
        priceRequest?.let {
            client.post("") {
                this.header("Content-Type", "application/json")
                this.body = priceRequest
            }
        } ?: 0.0
    }
}