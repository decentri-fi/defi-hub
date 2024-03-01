package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class CurveEthereumGaugeControllerContract(address: String) : EvmContract(address) {

    suspend fun getGaugeCount(): BigInteger {
        return readSingle("n_gauges", uint256())
    }

    suspend fun getGaugeTypes(addresses: List<String>): List<BigInteger> {
        return readMultiCall((addresses).map {
            createFunction(
                "gauge_types",
                listOf(it.toAddress()),
                listOf(uint128())
            )
        }).map {
            it.data[0].value as BigInteger
        }
    }

    suspend fun getGaugeAddresses(): List<String> {
        return readMultiCall((0 until getGaugeCount().toInt()).map {
            createFunction(
                "gauges",
                listOf(it.toBigInteger().toUint256()),
                listOf(address())
            )
        }).map {
            it.data[0].value as String
        }
    }
}