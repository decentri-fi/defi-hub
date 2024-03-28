package io.defitrack.port.output

import io.defitrack.adapter.output.domain.market.FarmingMarketInformationDTO
import io.defitrack.adapter.output.domain.market.LendingMarketInformationDTO
import io.defitrack.adapter.output.domain.market.PoolingMarketInformationDTO

interface MarketClient {

    suspend fun getPoolingMarkets(protocolSlug: String): List<PoolingMarketInformationDTO>
    suspend fun getFarmingMarkets(protocolSlug: String): List<FarmingMarketInformationDTO>
    suspend fun getLendingMarkets(protocol: String): List<LendingMarketInformationDTO>
}
