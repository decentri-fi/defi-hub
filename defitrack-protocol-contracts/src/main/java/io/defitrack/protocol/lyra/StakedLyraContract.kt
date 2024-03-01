package io.defitrack.protocol.lyra

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class StakedLyraContract(
    address: String
) : EvmContract(
    address
) {

    val stakedToken = constant<String>("STAKED_TOKEN", TypeUtils.address())
    val rewardToken = constant<String>("REWARD_TOKEN", TypeUtils.address())

    fun claimRewards(amount: BigInteger): (String) -> ContractCall {
        return {
            createFunction(
                "claimRewards",
                listOf(it.toAddress())
            )
        }
    }

    fun getTotalRewardsBalance(user: String): ContractCall {
        return createFunction(
            "getTotalRewardsBalance",
            user.toAddress().nel(),
            uint256().nel()
        )
    }
}