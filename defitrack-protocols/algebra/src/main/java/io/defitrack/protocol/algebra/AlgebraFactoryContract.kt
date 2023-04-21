package io.defitrack.protocol.algebra

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class AlgebraFactoryContract(
    blockchainGateway: BlockchainGateway, abi: String
) : EvmContract(
    blockchainGateway, "", abi
) {

    suspend fun getPoolByPair(token0: String, token1: String): String {
        return readWithoutAbi(
            "poolByPair",
            listOf(token0.toAddress(), token1.toAddress()),
            listOf(address())
        )[0].value as String
    }

}