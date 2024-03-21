package io.defitrack.protocol.sushiswap.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class SushiV2FactoryContract(
    address: String
) : EvmContract(address) {

    val allPairsLength = constant<BigInteger>("allPairsLength", uint256())

    suspend fun allPairs(): List<String> {
        val results = readMultiCall(
            (0 until allPairsLength.await().toInt()).map {
                createFunction(
                    "allPairs",
                    it.toUint256().nel(),
                    address().nel()
                )
            }
        )
        return results.filter {
            it.success
        }.map { it.data.first().value as String }
    }
}