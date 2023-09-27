package io.defitrack.evm.contract

import java.math.BigInteger

class GetEventLogsCommand(
    val addresses: List<String>,
    val topic: String,
    val optionalTopics: List<String?> = emptyList(),
    val fromBlock: BigInteger? = null,
    val toBlock: BigInteger? = null
)