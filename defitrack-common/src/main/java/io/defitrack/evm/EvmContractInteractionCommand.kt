package io.defitrack.evm

data class EvmContractInteractionCommand (
    val from: String?,
    val contract: String,
    val function: String
)