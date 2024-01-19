package io.defitrack.market.port.`in`

import io.defitrack.common.network.Network
import io.defitrack.market.domain.lending.LendingMarket

interface LendingMarkets : Markets<LendingMarket> {
    fun searchByToken(protocol: String, token: String, network: Network): List<LendingMarket>
}