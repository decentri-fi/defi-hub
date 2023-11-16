package io.defitrack.evm

import java.math.BigInteger

data class EvmContractInteractionCommand(
    val from: String?,
    val contract: String,
    val function: String,
    val block: BigInteger? = null
)