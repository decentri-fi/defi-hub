package io.defitrack.protocol.adamant

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway

class StrategyContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    val feeDistToken by lazy {
        readWithAbi("getFeeDistToken")[0].value as String
    }
}