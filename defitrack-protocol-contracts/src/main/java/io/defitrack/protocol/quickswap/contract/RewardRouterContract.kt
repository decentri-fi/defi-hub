package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class RewardRouterContract(
    address: String
) : EvmContract(address) {

    val feeQlpTracker = constant<String>("feeQlpTracker", TypeUtils.address())
    val qlp = constant<String>("qlp", TypeUtils.address())

    fun claimFn(user: String): ContractCall {
        return createFunction(
            "claim",
            listOf(user.toAddress()),
        )
    }
}