package io.defitrack.market.event

import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import java.math.BigDecimal

data class PoolMarketAddedEvent(
    val id: String,
    val price: BigDecimal,
    val address: String,
    val network: NetworkVO
) : MarketAddedEvent("pooling") {
    companion object {
        fun createPoolMarketAddedEvent(poolingMarket: PoolingMarket): PoolMarketAddedEvent {
            return PoolMarketAddedEvent(
                id = poolingMarket.id,
                price = poolingMarket.price.get(),
                address = poolingMarket.address,
                network = poolingMarket.network.toVO()
            )
        }
    }
}