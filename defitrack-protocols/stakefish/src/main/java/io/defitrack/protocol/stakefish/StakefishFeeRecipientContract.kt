package io.defitrack.protocol.stakefish

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class StakefishFeeRecipientContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, "", address) {

    fun getUserStateFunction(user: String): Function {
        return createFunction(
            "getUserState",
            listOf(user.toAddress()),
            listOf(uint256(), uint256(), uint256(), uint256())
        )
    }

    fun getPendingRewardFunction(user: String): Function {
        return createFunction(
            "pendingReward",
            listOf(user.toAddress()),
            listOf(uint256(), uint256())
        )
    }
}