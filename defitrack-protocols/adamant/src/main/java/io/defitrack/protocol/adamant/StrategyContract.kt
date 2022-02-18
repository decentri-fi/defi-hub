package io.defitrack.protocol.adamant

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor

class StrategyContract(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    val feeDistToken by lazy {
        read("getFeeDistToken")[0].value as String
    }
}