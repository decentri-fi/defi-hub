package io.defitrack.lending

import io.defitrack.lending.domain.LendingMarketElement
import io.codechef.defitrack.protocol.ProtocolService

interface LendingMarketService : ProtocolService {
    fun getLendingMarkets(): List<LendingMarketElement>
}