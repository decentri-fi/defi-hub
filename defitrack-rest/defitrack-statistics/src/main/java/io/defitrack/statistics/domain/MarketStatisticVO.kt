package io.defitrack.statistics.domain

class MarketStatisticVO(
    val total: Int,
    val marketsPerProtocol: Map<String, Int>
)