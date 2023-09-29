package io.defitrack.market.farming

import io.defitrack.claimable.Claimable

interface ClaimableMarketProvider {

    suspend fun getClaimables(): List<Claimable>

}