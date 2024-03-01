package io.defitrack.protocol.velodrome.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class PoolFactoryContract(
    contractAddress: String
) : EvmContract(
    contractAddress
) {
    suspend fun allPairsLength(): BigInteger {
        return readSingle("allPoolsLength", uint256())
    }

    suspend fun allPools(): List<String> {
        val functions = (0 until allPairsLength().toInt()).map { poolIndex ->
            createFunction(
                "allPools",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(address())
            )
        }

        val results = readMultiCall(functions)
        return results.map { it.data[0].value as String }
    }
}