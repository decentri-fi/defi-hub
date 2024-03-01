package io.defitrack.uniswap.v2

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class PairFactoryContract(
    contractAddress: String
) : EvmContract(contractAddress) {
    suspend fun allPairsLength(): BigInteger {
        return readSingle("allPairsLength", uint256())
    }

    suspend fun allPairs(): List<String> {
        val functions = (0 until allPairsLength().toInt()).map { poolIndex ->
            createFunction(
                "allPairs",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    TypeUtils.address(),
                )
            )
        }

        val results = readMultiCall(functions)
        return results.map { it.data[0].value as String }
    }

}