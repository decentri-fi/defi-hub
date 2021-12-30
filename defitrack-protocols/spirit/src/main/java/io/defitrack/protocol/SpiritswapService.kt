package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiUser
import io.defitrack.protocol.sushi.domain.SushiswapPair

interface SpiritswapService {
    fun getPairs(): List<SushiswapPair>
    fun getPairDayData(pairId: String): List<PairDayData>
    fun getUserPoolings(user: String): List<SushiUser>
    fun getNetwork(): Network
}