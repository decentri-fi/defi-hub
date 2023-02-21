package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import java.math.BigInteger

class PairFactoryContract(
    blockchainGateway: BlockchainGateway,
    contractAddress: String
) : EvmContract(
    blockchainGateway, "", contractAddress
) {
    suspend fun allPairsLength(): BigInteger {
        return readWithoutAbi(
            "allPairsLength",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun allPairs(): List<String> {
        val multicalls = (0 until allPairsLength().toInt()).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "allPairs",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        TypeUtils.address(),
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { it[0].value as String }
    }

}