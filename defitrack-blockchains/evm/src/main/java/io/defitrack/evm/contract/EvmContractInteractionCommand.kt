package io.defitrack.evm.contract

class EvmContractInteractionCommand (
    val from: String?,
    val contract: String,
    val function: String
): ContractInteractionCommand