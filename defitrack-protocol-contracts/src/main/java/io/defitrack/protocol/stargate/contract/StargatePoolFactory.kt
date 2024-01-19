package io.defitrack.protocol.stargate.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class StargatePoolFactory(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway,address
) {

    suspend fun poolLength(): Int {
        return (read(
            "allPoolsLength",
            outputs = listOf(uint256())
        )[0].value as BigInteger).toInt()
    }

    suspend fun getPools(): List<String> {
        val functions = (0 until poolLength()).map { poolIndex ->
            createFunction(
                "allPools",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(TypeUtils.address())
            )
        }
        val results = readMultiCall(functions)
        return results.map { retVal ->
            retVal.data[0].value as String
        }
    }
}