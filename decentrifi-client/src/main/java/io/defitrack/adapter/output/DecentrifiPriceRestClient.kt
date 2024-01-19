package io.defitrack.adapter.output

import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.out.Prices
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
internal class DecentrifiPriceRestClient(
    @Value("\${priceResourceLocation:http://defitrack-price:8080}") val priceResourceLocation: String,
    private val client: HttpClient
)  : Prices {

    private val logger = LoggerFactory.getLogger(this::class.java)

   override suspend fun calculatePrice(getPriceCommand: GetPriceCommand): Double {
        return with(getPriceCommand) {
            try {
                withContext(Dispatchers.IO) {
                    val post = client.post(priceResourceLocation) {
                        header("Content-Type", "application/json")
                        setBody(getPriceCommand)
                    }
                    if (post.status.isSuccess()) {
                        post.body()
                    } else {
                        logger.error("unable to fetch price for ${address} on network ${network} (status: ${post.status})")
                        0.0
                    }
                }
            } catch (ex: Exception) {
                logger.error("unable to fetch price for ${address} on network ${network}")
                0.0
            }
        } ?: 0.0
    }
}