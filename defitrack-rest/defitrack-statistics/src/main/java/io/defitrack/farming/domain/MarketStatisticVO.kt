package io.defitrack.farming.domain

class MarketStatisticVO(
    val total: Int,
    val marketsPerProtocol: Map<String, Int>
)