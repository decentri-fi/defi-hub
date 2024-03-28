package io.defitrack.price.application

import io.defitrack.common.network.Network
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

@Component
class AggregatedPriceProvider(
    private val priceAggregator: PriceAggregator,
) {

    val priceCache = Cache.Builder<String, BigDecimal>().expireAfterWrite(5.minutes).build()

    suspend fun getPrice(address: String, network: Network): BigDecimal {
        return priceCache.get("${address.lowercase()}-${network.slug}") {
            priceAggregator.getPrice(address, network.name.lowercase())?.price
                ?: BigDecimal.ZERO
        }
    }
}