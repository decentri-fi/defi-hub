package io.defitrack.statistics.domain

import io.defitrack.protocol.ProtocolInformation

class MarketStatisticVO(
    val total: Int,
    val marketsPerProtocol: Map<ProtocolInformation, Int>
)