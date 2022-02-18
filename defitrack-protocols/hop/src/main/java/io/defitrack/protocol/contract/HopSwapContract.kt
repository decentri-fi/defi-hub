package io.defitrack.protocol.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
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