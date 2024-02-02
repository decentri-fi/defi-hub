package io.defitrack.protocol.plutusdao

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

class PlutusRouterContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {


    fun claimEsPls(): ContractCall {
        return createFunction(
            "claimEsPls",
            emptyList(),
            emptyList()
        )
    }
}