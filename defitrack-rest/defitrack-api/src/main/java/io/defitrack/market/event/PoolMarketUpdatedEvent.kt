package io.defitrack.market.event

import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import java.math.BigDecimal

data class PoolMarketUpdatedEvent(
    val id: String,
    val price: BigDecimal,
    val address: String,
    val network: NetworkVO
) : MarketUpdatedEvent("pooling") {
    companion object {
        fun createPoolMarketAddedEvent(poolingMarket: PoolingMarket): PoolMarketUpdatedEvent {
            return PoolMarketUpdatedEvent(
                id = poolingMarket.id,
                price = poolingMarket.price.get(),
                address = poolingMarket.address,
                network = poolingMarket.network.toVO()
            )
        }
    }
}