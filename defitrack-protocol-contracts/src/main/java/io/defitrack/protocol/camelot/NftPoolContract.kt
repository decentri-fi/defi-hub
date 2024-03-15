package io.defitrack.protocol.camelot

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class NftPoolContract(blockchainGateway: BlockchainGateway, address: String) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    suspend fun getLpToken(): PoolInfo {
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
        ).run {
            PoolInfo(
                lpToken = this[0].value as String,
                lpSupply = this[5].value as BigInteger
            )
        }
    }

    data class PoolInfo(
        val lpToken: String,
        val lpSupply: BigInteger
    )

}