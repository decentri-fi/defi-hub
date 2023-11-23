package io.defitrack.protocol.mycelium

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

class RewardRouter02Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, address) {

    val stakedMlpTracker = constant<String>("stakedMlpTracker", address())
    val feeMlpTracker = constant<String>("feeMlpTracker", address())
    val stakedMycTracker = constant<String>("stakedMycTracker", address())
    val bonusMycTracker = constant<String>("bonusMycTracker", address())
    val mlp = constant<String>("mlp", address())
    val esMyc = constant<String>("esMyc", address())
    val bnMyc = constant<String>("bnMyc", address())

    fun claim(): ContractCall {
        return createFunction(
            "claim",
            emptyList(),
            emptyList()
        )
    }
}