package io.defitrack.protocol.equalizer

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class EqualizerVoter(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val length = constant<BigInteger>("length", uint256())

    suspend fun pools(): List<String> {
        return readMultiCall(
            (0 until length.await().toInt()).map {
                createFunction(
                    "pools",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(address())
                )
            }
        ).filter {
            it.success
        }.map { it.data[0].value as String }
    }

    suspend fun gauges(pools: List<String>) {
        readMultiCall(
            pools.map {
                createFunction(
                    "gauges",
                    listOf(it.toAddress()),
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