package io.defitrack.protocol.stakefish

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class StakefishFeeRecipientContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    fun claimFunction(user: String): Function {
        return createFunction(
            "collectReward", listOf(
                user.toAddress(), BigInteger.ZERO.toUint256()
            ), emptyList()
        )
    }

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