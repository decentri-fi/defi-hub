package io.defitrack.marketinfo.port.out

import io.defitrack.market.domain.farming.FarmingMarketInformation
import io.defitrack.market.domain.lending.LendingMarketInformation
import io.defitrack.market.domain.pooling.PoolingMarketInformation


interface Markets {
    suspend fun getPoolingMarkets(protocolSlug: String): List<PoolingMarketInformation>
    suspend fun getFarmingMarkets(protocolSlug: String): List<FarmingMarketInformation>
    suspend fun getLendingMarkets(protocol: String): List<LendingMarketInformation>
}