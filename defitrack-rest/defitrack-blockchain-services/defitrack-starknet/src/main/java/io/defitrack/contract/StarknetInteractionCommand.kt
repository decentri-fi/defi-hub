package io.defitrack.contract

import io.defitrack.evm.contract.ContractInteractionCommand

class StarknetInteractionCommand(
    val contractAddress: String,
    val entryPointSelector: String,
    val calldata: List<String>
) : ContractInteractionCommand