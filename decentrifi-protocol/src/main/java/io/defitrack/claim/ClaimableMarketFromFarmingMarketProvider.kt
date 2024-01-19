package io.defitrack.claim

import io.defitrack.market.port.out.FarmingMarketProvider
import org.springframework.stereotype.Component

@Component
class ClaimableMarketFromFarmingMarketProvider(
    private val farmingMarketProvider: List<FarmingMarketProvider>
) : AbstractClaimableMarketProvider() {

    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        return farmingMarketProvider.flatMap {
            it.getMarkets()
        }.filter {
            it.claimableRewardFetchers.isNotEmpty()
        }.map {
            ClaimableMarket(
                id = "rwrd_" + it.id,
                name = it.name + " reward",
                network = it.network,
                protocol = it.protocol,
                claimableRewardFetchers = it.claimableRewardFetchers,
            )
        }
    }
}