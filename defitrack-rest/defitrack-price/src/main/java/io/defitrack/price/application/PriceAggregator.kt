package io.defitrack.price.application

import io.defitrack.price.external.domain.ExternalPrice
import org.springframework.stereotype.Component

@Component
class PriceAggregator {

    val price: MutableMap<String, ExternalPrice> = mutableMapOf()

    fun addPrice(it: ExternalPrice) {
        price["${it.address.lowercase()}-${it.network.name.lowercase()}"] = it
    }

    fun getPrice(address: String, network: String): ExternalPrice? {
        return price["${address.lowercase()}-${network}"]
    }

    fun getAllPrices(): List<ExternalPrice> {
        return price.values.toList().sortedBy {
            it.address
        }
    }
}