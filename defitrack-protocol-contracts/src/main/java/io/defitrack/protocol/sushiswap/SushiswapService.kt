package io.defitrack.protocol.sushiswap

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushiswap.domain.PairDayData
import io.defitrack.protocol.sushiswap.domain.SushiUser
import io.defitrack.protocol.sushiswap.domain.SushiswapPair

interface SushiswapService {
    suspend fun getPairs(): List<SushiswapPair>
    fun getNetwork(): Network
}