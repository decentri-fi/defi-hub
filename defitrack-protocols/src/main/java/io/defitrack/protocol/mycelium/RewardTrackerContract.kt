package io.defitrack.protocol.mycelium

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class RewardTrackerContract(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {

    val rewardToken = constant<String>("rewardToken", TypeUtils.address())

    fun stakedAmounts(user: String): Function {
        return createFunction(
            "stakedAmounts",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimable(user: String): Function {
        return createFunction(
            "claimable",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}