package io.defitrack.protocol.crv.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class CurveGaugeControllerContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun getGaugeCount(): BigInteger {
        return readWithoutAbi(
            "n_gauges",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun getGaugeAddresses(): List<String> {
        return (0 until getGaugeCount().toInt()).map {
            readWithoutAbi(
                "gauges",
                listOf(it.toBigInteger().toUint256()),
                listOf(address())
            )[0].value as String
        }
    }
}