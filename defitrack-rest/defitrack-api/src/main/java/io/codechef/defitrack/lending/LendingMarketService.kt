package io.codechef.defitrack.lending

import io.codechef.defitrack.lending.domain.LendingMarketElement
import io.codechef.defitrack.protocol.ProtocolService

interface LendingMarketService : ProtocolService {
    fun getLendingMarkets(): List<LendingMarketElement>
}