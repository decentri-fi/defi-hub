package io.defitrack.statistics.domain

import io.defitrack.protocol.ProtocolVO

class MarketStatisticVO(
    val total: Int,
    val marketsPerProtocol: Map<ProtocolVO, Int>
)