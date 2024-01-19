package io.defitrack.market.adapter.`in`.resource

import java.math.BigInteger

data class GetHistoryRequest(
    val network: String,
    val fromBlock: BigInteger?,
    val toBlock: BigInteger?,
    val markets: List<String>? = null
)