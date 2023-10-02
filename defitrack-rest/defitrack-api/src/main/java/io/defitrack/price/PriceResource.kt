package io.defitrack.price

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    val cache = Cache.Builder<String, BigDecimal>()
        .expireAfterWrite(10.minutes)
        .build()

    suspend fun calculatePrice(priceRequest: PriceRequest?): Double {
        return priceRequest?.let {
            try {
                withContext(Dispatchers.IO) {
                    val post = client.post(priceResourceLocation) {
                        this.header("Content-Type", "application/json")
                        setBody(priceRequest)
                    }
                    if (post.status.isSuccess()) {
                        post.body()
                    } else {
                        logger.error("unable to fetch price for ${it.address} on network ${priceRequest.network} (status: ${post.status})")
                        0.0
                    }
                }
            } catch (ex: Exception) {
                logger.error("unable to fetch price for ${it.address} on network ${priceRequest.network}")
                0.0
            }
        } ?: 0.0
    }
}