package io.defitrack.protocol.contract

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import java.math.BigInteger

class HopSwapContract(
    evmContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(evmContractAccessor, abi, address) {

    val virtualPrice by lazy {
        read("getVirtualPrice")[0].value as BigInteger
    }
}