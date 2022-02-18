package io.defitrack.protocol.crv

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Int128
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CrvPolygonGaugeControllerContract(
    ethereumContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
) : EvmContract(
    ethereumContractAccessor,
    abi,
    address
) {

    fun numberOfGauges(): Int {
        return (read(
            "n_gauges",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Int128::class.java)
            )
        )[0].value as BigInteger).toInt()
    }

    fun getGauge(gaugeNumber: Int): String {
        return read(
            "gauges",
            inputs = listOf(
                BigInteger.valueOf(gaugeNumber.toLong()).toUint256(),
            ),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}

class CrvPolygonGauge(
    ethereumContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
) : EvmContract(ethereumContractAccessor, abi, address) {

    val name: String by lazy {
        read(
            "name",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Utf8String::class.java)
            )
        )[0].value as String
    }

    val symbol: String by lazy {
        read(
            "symbol",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Utf8String::class.java)
            )
        )[0].value as String
    }

    val controller by lazy {
        read("controller", outputs = listOf(TypeReference.create(Address::class.java)))[0].value as String
    }

    val decimals: BigInteger by lazy {
        read(
            "decimals",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val lpToken: String by lazy {
        read(
            "lp_token",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}

