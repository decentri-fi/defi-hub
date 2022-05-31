package io.defitrack.protocol.beefy.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address

class StrategyPolygonQuickLPContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) :
    EvmContract(solidityBasedContractAccessor, abi, address) {

    val rewardPool by lazy {
        readWithAbi(
            "rewardPool",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }
}