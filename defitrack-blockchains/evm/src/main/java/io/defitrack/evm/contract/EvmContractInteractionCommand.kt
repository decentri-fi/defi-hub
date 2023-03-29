package io.defitrack.evm.contract

data class EvmContractInteractionCommand (
    val from: String?,
    val contract: String,
    val function: String
): ContractInteractionCommand