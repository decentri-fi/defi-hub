package io.defitrack.claimable

import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.market.farming.FarmingMarketProvider
import org.springframework.stereotype.Component

@Component
class ClaimableMarketFromFarmingMarketProvider(
    private val farmingMarketProvider: List<FarmingMarketProvider>
) : ClaimableMarketProvider() {

    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        return farmingMarketProvider.flatMap {
            it.getMarkets()
        }.filter {
            it.claimableRewardFetchers != null
        }.map {
            ClaimableMarket(
                id = "rwrd_" + it.id,
                name = it.name + " reward",
                network = it.network,
                protocol = it.protocol,
                claimableRewardFetchers = it.claimableRewardFetchers!!,
            )
        }
    }
}