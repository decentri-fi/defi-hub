package io.codechef.defitrack.pool.contract

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class LPTokenContract(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) :
    EvmContract(solidityBasedContractAccessor, abi, address) {

    val name by lazy {
        read("name")[0].value as String
    }

    val symbol by lazy {
        read("symbol")[0].value as String
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

    val token0 by lazy {
        read("token0")[0].value as String
    }

    val token1 by lazy {
        read("token1")[0].value as String
    }

    val totalSupply by lazy {
        read("totalSupply")[0].value as BigInteger
    }
}