package io.defitrack.protocol.camelot

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class NftPoolContract(address: String) : EvmContract(address) {

    suspend fun getLpToken(): PoolInfo {
        return read(
            "getPoolInfo",
            emptyList(),
            listOf(
                address(),
                address(),
                address(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
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