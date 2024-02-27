package io.defitrack.protocol.hop.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class HopStakingRewardContract(address: String) : ERC20Contract(address) {

    val rewardsToken = constant<String>("rewardsToken", address())
    val stakingToken = constant<String>("stakingToken", address())

    fun earnedFn(address: String): ContractCall {
        return createFunction(
            "earned",
            inputs = listOf(
                address.toAddress()
            ),
            outputs = listOf(
                uint256()
            )
        )
    }

    fun getRewardFn(): ContractCall {
        return createFunction("getReward")
    }


}