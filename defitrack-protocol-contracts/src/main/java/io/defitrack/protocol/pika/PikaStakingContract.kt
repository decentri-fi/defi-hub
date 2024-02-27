package io.defitrack.protocol.pika

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.*
import java.math.BigInteger

context(BlockchainGateway)
class PikaStakingContract(
    address: String
) : ERC20Contract(address) {

    val stakingToken = constant<String>("stakingToken", TypeUtils.address())

    suspend fun rewardPools(): List<PikaRewards> {
        val result = readMultiCall(
            listOf(
                createFunction(
                    "rewardPools",
                    listOf(0.toBigInteger().toUint256()),
                    listOf(TypeUtils.address())
                ),
                createFunction(
                    "rewardPools",
                    listOf(1.toBigInteger().toUint256()),
                    listOf(TypeUtils.address())
                )
            )
        )

        return with(this) {
            listOf(
                PikaRewardPoolContract(

                    (result[0].data[0].value as String)
                ),
                PikaTokenRewardsPoolContract(
                    (result[1].data[0].value as String)
                ),
            )
        }
    }

    interface PikaRewards {
        fun getClaimableReward(user: String): ContractCall
        fun claimRewardFn(user: String): ContractCall

        suspend fun getRewardToken(): String

        fun fetchAddress(): String
    }

    context(BlockchainGateway)
    class PikaRewardPoolContract(
        address: String
    ) : EvmContract(address), PikaRewards {
        val rewardToken = constant<String>("rewardToken", TypeUtils.address())
        val precision = constant<BigInteger>("PRECISION", TypeUtils.uint256())

        suspend fun getPrecision(): BigInteger {
            return precision.await()
        }


        override fun claimRewardFn(user: String): ContractCall {
            return createFunction(
                "claimReward",
                listOf(user.toAddress()),
                emptyList()
            )
        }

        override suspend fun getRewardToken(): String {
            return rewardToken.await()
        }

        override fun fetchAddress(): String {
            return address
        }

        override fun getClaimableReward(user: String): ContractCall {
            return createFunction(
                "getClaimableReward",
                listOf(user.toAddress()),
                listOf(TypeUtils.uint256())
            )
        }
    }

    context(BlockchainGateway)
    class PikaTokenRewardsPoolContract(
        address: String
    ) : EvmContract(address), PikaRewards {
        val rewardToken = constant<String>("rewardToken", TypeUtils.address())

        override fun claimRewardFn(user: String): ContractCall {
            return createFunction(
                "getReward",
                emptyList(),
                emptyList()
            )
        }

        override suspend fun getRewardToken(): String {
            return rewardToken.await()
        }

        override fun getClaimableReward(user: String): ContractCall {
            return createFunction(
                "rewards",
                listOf(user.toAddress()),
                listOf(TypeUtils.uint256())
            )
        }

        override fun fetchAddress(): String {
            return address
        }

    }
}