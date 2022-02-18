package io.defitrack.protocol.beefy.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address

class StrategyPolygonQuickLPContract(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) :
    EvmContract(solidityBasedContractAccessor, abi, address) {

    val rewardPool by lazy {
        read(
            "rewardPool",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }
}