package io.defitrack.protocol.camelot

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

class NftPoolContract(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {

    suspend fun getLpToken(): String {
        return read(
            "getPoolInfo",
            emptyList(),
            listOf(
                TypeUtils.address(),
                TypeUtils.address(),
                TypeUtils.address(),
                TypeUtils.uint256(),
                TypeUtils.uint256(),
                TypeUtils.uint256(),
                TypeUtils.uint256(),
                TypeUtils.uint256(),
            )
        )[0].value as String
    }

}