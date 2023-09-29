package io.defitrack.market.farming

import io.defitrack.claimable.ClaimableMarket

interface ClaimableMarketProvider {

    suspend fun getClaimables(): List<ClaimableMarket>

}