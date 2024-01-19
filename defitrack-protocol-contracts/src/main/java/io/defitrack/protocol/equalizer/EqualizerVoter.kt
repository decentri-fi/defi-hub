package io.defitrack.protocol.equalizer

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
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

    suspend fun gauges(pools: List<String>): Map<String, Option<String>> {
        return readMultiCall(
            pools.map {
                createFunction(
                    "gauges",
                    listOf(it.toAddress()),
                    listOf(address())
                )
            }
        ).mapIndexed { index, it ->
            pools[index] to if (it.success) {
                Some(it.data[0].value as String).map(String::lowercase)
            } else {
                None
            }
        }.toMap()
    }
}