package io.defitrack.market.pooling.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class LPTokenContract(
    solidityBasedContractAccessor: BlockchainGateway,
    address: String
) : ERC20Contract(solidityBasedContractAccessor, address) {

    suspend fun token0(): String {
        return readSingle("token0", TypeUtils.address())
    }

    suspend fun token1(): String {
        return readSingle("token1", TypeUtils.address())
    }
}