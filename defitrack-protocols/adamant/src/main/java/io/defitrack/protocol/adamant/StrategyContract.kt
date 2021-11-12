package io.defitrack.protocol.adamant

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor

class StrategyContract(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    val feeDistToken by lazy {
        read("getFeeDistToken")[0].value as String
    }
}