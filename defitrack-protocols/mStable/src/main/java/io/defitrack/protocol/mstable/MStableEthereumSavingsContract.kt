package io.defitrack.protocol.mstable

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class MStableEthereumSavingsContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : ERC20Contract(ethereumContractAccessor, abi, address) {

    val underlying: String by lazy {
        readWithAbi(
            "underlying"
        )[0].value as String
    }
}