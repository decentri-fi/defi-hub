package io.defitrack.protocol.plutusdao

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class PlutusRouterContract(
    address: String
) : EvmContract(
    address
) {


    fun claimEsPls(): ContractCall {
        return createFunction(
            "claimEsPls",
            emptyList(),
            emptyList()
        )
    }
}