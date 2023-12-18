package io.defitrack.market.pooling.history

import java.math.BigInteger

data class GetHistoryRequest(
    val network: String,
    val fromBlock: BigInteger?,
    val toBlock: BigInteger?,
    val markets: List<String>? = null
)