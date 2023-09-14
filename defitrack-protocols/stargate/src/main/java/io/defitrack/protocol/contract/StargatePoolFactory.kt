package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class StargatePoolFactory(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun poolLength(): Int {
        return (read(
            "allPoolsLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
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