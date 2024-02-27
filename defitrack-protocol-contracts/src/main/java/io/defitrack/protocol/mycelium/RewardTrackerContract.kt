package io.defitrack.protocol.mycelium

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class RewardTrackerContract(address: String) : ERC20Contract(
    address
) {

    val rewardToken = constant<String>("rewardToken", TypeUtils.address())

    fun stakedAmounts(user: String): ContractCall {
        return createFunction(
            "stakedAmounts",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimable(user: String): ContractCall {
        return createFunction(
            "claimable",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}