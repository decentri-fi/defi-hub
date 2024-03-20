package io.defitrack.price.application

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

@Component
class AggregatedPriceProvider(
    private val priceAggregator: PriceAggregator,
) {

    val priceCache = Cache.Builder<String, BigDecimal>().expireAfterWrite(5.minutes).build()

    suspend fun getPrice(token: FungibleTokenInformation): BigDecimal {
        return priceCache.get("${token.address.lowercase()}-${token.network.toNetwork().slug}") {
            priceAggregator.getPrice(token.address, token.network.name.lowercase())?.price
                ?: BigDecimal.ZERO
        }
    }
}