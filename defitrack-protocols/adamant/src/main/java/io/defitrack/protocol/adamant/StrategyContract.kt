package io.defitrack.protocol.adamant

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityContract

class StrategyContract(
    solidityBasedContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String
) : SolidityContract(solidityBasedContractAccessor, abi, address) {

    val feeDistToken by lazy {
        read("getFeeDistToken")[0].value as String
    }
}