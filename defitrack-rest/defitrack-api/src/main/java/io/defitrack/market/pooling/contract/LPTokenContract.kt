package io.defitrack.market.pooling.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class LPTokenContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : ERC20Contract(solidityBasedContractAccessor, abi, address) {

    suspend fun token0(): String {
        return readWithAbi("token0")[0].value as String
    }

    suspend fun token1(): String {
        return readWithAbi("token1")[0].value as String
    }
}