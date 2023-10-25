package io.defitrack.protocol.equalizer

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class EqualizerGaugeContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val stake = constant<String>("stake", TypeUtils.address())
    val rewardsListLength = constant<BigInteger>("rewardsListLength", uint256())

    fun earnedFn(rewardsToken: String, user: String): Function {
        return createFunction(
            "earned",
            listOf(rewardsToken.toAddress(), user.toAddress()),
            listOf(uint256())
        )
    }

    fun earnedFnFor(rewardsToken: String): (String) -> Function {
        return  {user: String ->
            earnedFn(rewardsToken, user)
        }
    }

    fun getRewardFn(): ContractCall {
        return createFunction(
            "getReward",
            listOf(),
            listOf()
        ).toContractCall()
    }

    suspend fun getRewards(): List<String> {
        return readMultiCall(
            (0 until rewardsListLength.await().toInt()).map {
                createFunction(
                    "rewardTokens",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(TypeUtils.address())
                )
            }
        ).filter {
            it.success
        }.map {
            it.data[0].value as String
        }
    }
}