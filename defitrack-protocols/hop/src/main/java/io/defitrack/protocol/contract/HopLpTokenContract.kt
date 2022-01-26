package io.defitrack.protocol.contract

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class HopLpTokenContract(
    evmContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(evmContractAccessor, abi, address) {

    val name by lazy {
        read("name")[0].value as String
    }

    val symbol by lazy {
        read("symbol")[0].value as String
    }

    val swap by lazy {
        read("swap")[0].value as String
    }

    fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val decimals by lazy {
        (read("decimals")[0].value as BigInteger).toInt()
    }

    val totalSupply by lazy {
        read("totalSupply")[0].value as BigInteger
    }
}