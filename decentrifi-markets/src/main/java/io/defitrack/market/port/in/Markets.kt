package io.defitrack.market.port.`in`

import io.defitrack.market.domain.DefiMarket

interface Markets<T: DefiMarket> {
    fun getAllMarkets(protocol: String, network: String? = null): List<T>
}