package io.defitrack.protocol.camelot

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class PoolFactoryContract(address: String) : EvmContract(address) {

    val poolsLength = constant<BigInteger>("poolsLength", uint256())

    suspend fun getStakingPools(): List<String> {
        return readMultiCall(
            (0 until poolsLength.await().toInt()).map {
                createFunction(
                    "pools",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(address())
                )
            }
        ).filter {
            it.success
        }.map {
            it.data[0].value as String
        }
    }

}