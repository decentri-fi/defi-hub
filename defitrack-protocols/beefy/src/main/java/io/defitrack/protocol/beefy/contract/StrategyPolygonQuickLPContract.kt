package io.defitrack.protocol.beefy.contract

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
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