package io.defitrack.protocol.plutusdao

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class PlutusRouterContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
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