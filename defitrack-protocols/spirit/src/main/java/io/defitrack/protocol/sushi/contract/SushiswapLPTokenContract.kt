package io.defitrack.protocol.sushi.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import java.math.BigInteger

class SushiswapLPTokenContract(
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