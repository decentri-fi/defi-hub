package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiUser
import io.defitrack.protocol.sushi.domain.SushiswapPair

interface SpiritswapService {
    suspend fun getPairs(): List<SushiswapPair>
    suspend fun getPairDayData(pairId: String): List<PairDayData>
    suspend fun getUserPoolings(user: String): List<SushiUser>
    fun getNetwork(): Network
}