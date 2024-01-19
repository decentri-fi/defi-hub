package io.defitrack.market.port.`in`

import io.defitrack.common.network.Network
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.farming.FarmingMarket

interface PoolingMarkets : Markets<PoolingMarket> {

    suspend fun searchByToken(protocol: String, tokenAddress: String, network: Network): List<PoolingMarket>
    suspend fun findAlternatives(protocol: String, tokenAddress: String, network: Network): List<PoolingMarket>
}