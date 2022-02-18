package io.defitrack.protocol.mstable

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import java.math.BigInteger

class MStableEthereumSavingsContract(
    ethereumContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(ethereumContractAccessor, abi, address) {

    val symbol: String by lazy {
        read(
            "symbol"
        )[0].value as String
    }

    val underlying: String by lazy {
        read(
            "underlying"
        )[0].value as String
    }

    val name: String by lazy {
        read(
            "name"
        )[0].value as String
    }


    val decimals: Int by lazy {
        (read(
            "decimals"
        )[0].value as BigInteger).toInt()
    }
}