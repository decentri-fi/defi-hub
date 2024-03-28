package io.defitrack.market.port.`in`

import io.defitrack.common.network.Network
import io.defitrack.market.domain.farming.FarmingMarket

interface FarmingMarkets : Markets<FarmingMarket> {

    suspend fun searchByToken(
        protocol: String,
        tokenAddress: String,
        network: Network
    ): List<FarmingMarket>

    fun getStakingMarketById(id: String): FarmingMarket?
}