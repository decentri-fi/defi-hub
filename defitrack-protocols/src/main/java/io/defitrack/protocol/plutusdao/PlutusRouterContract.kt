package io.defitrack.protocol.plutusdao

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class PlutusRouterContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {


    fun claimEsPls(): MutableFunction {
        return createFunction(
            "claimEsPls",
            emptyList(),
            emptyList()
        ).toMutableFunction()
    }
}