package io.defitrack.protocol.stakefish

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

context(BlockchainGateway)
class StakefishFeeRecipientContract(
    address: String
) : EvmContract(address) {

    fun claimFunction(user: String): ContractCall {
        return createFunction(
            "collectReward", listOf(
                user.toAddress(), BigInteger.ZERO.toUint256()
            ), emptyList()
        )
    }

    fun getUserStateFunction(user: String): ContractCall {
        return createFunction(
            "getUserState",
            listOf(user.toAddress()),
            listOf(uint256(), uint256(), uint256(), uint256())
        )
    }

    fun getPendingRewardFunction(user: String): ContractCall {
        return createFunction(
            "pendingReward",
            listOf(user.toAddress()),
            listOf(uint256(), uint256())
        )
    }

    data class PoolState(
        val lastRewardUpdateBlock: BigInteger,
        val accRewardPerValidator: BigInteger,
        val validatorCount: BigInteger,
        val lifetimeCollectedCommission: BigInteger,
        val lifetimePaidUserRewards: BigInteger,
        val amountTransferredToColdWallet: BigInteger,
        val isOpenForWithdrawal: Boolean
    )

    data class UserState(
        val validatorCount: BigInteger,
        val lifetimeCredit: BigInteger,
        val debit: BigInteger,
        val collectedRewards: BigInteger
    )

}