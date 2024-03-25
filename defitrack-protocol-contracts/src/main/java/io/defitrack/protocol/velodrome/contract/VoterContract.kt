package io.defitrack.protocol.velodrome.contract

import arrow.core.*
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract
import io.defitrack.evm.contract.EvmContract


context(BlockchainGateway)
class VoterContract(
    address: String
) : EvmContract(
    address
) {

    suspend fun gauges(poolAddress: String): String {
        return read(
            "gauges",
            inputs = listOf(poolAddress.toAddress()),
            outputs = listOf(address())
        )[0].value as String
    }

    suspend fun gaugesFor(poolAddresses: List<String>): List<Option<String>> {
        return readMultiCall(
            poolAddresses.map {
                createFunction(
                    "gauges",
                    inputs = listOf(it.toAddress()),
                    outputs = listOf(address())
                )
            }
        ).map {
            if (it.success) {
                (it.data[0].value as String).some()
            } else {
                none()
            }
        }
    }
}