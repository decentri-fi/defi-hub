package io.defitrack.protocol.traderjoe

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class StableJoeStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val joe = constant<String>("joe", TypeUtils.address())
    val rewardTokensLength = constant<BigInteger>("rewardTokensLength", TypeUtils.uint256())

    fun getUserInfofn(user: String, reward: String): Function {
        return createFunction(
            "getuserInfo",
            listOf(user.toAddress(), reward.toAddress()),
            listOf(
                uint256(), uint256()
            )
        )
    }

    fun pendingRewardFn(user: String, reward: String): Function {
        return createFunction(
            "pendingReward",
            listOf(user.toAddress(), reward.toAddress()),
            listOf(
                uint256()
            )
        )
    }

    fun harvest(): ContractCall {
        return createFunction(
            "deposit",
            listOf(BigInteger.ZERO.toUint256())
        ).toContractCall()
    }

    suspend fun rewardTokens(): List<String> {
        return readMultiCall(
            (0 until rewardTokensLength.await().toInt()).map {
                createFunction(
                    "rewardTokens",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(TypeUtils.address())
                )
            }
        ).filter {
            it.success
        }.map { it.data[0].value as String }
    }

}