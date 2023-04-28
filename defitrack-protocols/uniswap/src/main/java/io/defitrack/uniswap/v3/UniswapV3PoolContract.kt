package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class UniswapV3PoolContract(
    blockchaingateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchaingateway, "", address
) {

    suspend fun token0(): String {
        return readSingle("token0", TypeUtils.address())
    }

    suspend fun token1(): String {
        return readSingle("token1", TypeUtils.address())
    }

}