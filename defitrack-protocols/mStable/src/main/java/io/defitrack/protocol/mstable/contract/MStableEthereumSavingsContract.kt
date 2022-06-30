package io.defitrack.protocol.mstable.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class MStableEthereumSavingsContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : ERC20Contract(ethereumContractAccessor, abi, address) {

    suspend fun underlying(): String {
        return readWithAbi(
            "underlying"
        )[0].value as String
    }
}