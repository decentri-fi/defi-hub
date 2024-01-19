package io.defitrack.protocol.pika

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class PikaStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
) {

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

        return listOf(
            PikaRewardPoolContract(
                blockchainGateway,
                (result[0].data[0].value as String)
            ),
            PikaTokenRewardsPoolContract(
                blockchainGateway,
                (result[1].data[0].value as String)
            ),
        )
    }

    interface PikaRewards {
        fun getClaimableReward(user: String): ContractCall
        fun claimRewardFn(user: String): ContractCall

        suspend fun getRewardToken(): String

        fun fetchAddress(): String
    }

    class PikaRewardPoolContract(
        blockchainGateway: BlockchainGateway,
        address: String
    ) : EvmContract(blockchainGateway, address), PikaRewards {
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

    class PikaTokenRewardsPoolContract(
        blockchainGateway: BlockchainGateway,
        address: String
    ) : EvmContract(blockchainGateway, address), PikaRewards {
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