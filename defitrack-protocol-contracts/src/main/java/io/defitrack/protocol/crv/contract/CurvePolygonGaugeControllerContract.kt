package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class CurvePolygonGaugeControllerContract(address: String) : EvmContract(address) {

    suspend fun getGaugeCount(): BigInteger {
        return readSingle("get_gauge_count", uint256())
    }

    suspend fun getGaugeAddresses(): List<String> {
        return readMultiCall((0 until getGaugeCount().toInt()).map {
            createFunction(
                "get_gauge",
                listOf(it.toBigInteger().toUint256()),
                listOf(address())
            )
        }).filter {
            it.success
        }.map {
            it.data[0].value as String
        }
    }
}