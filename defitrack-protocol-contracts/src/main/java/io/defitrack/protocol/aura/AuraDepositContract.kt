package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
class AuraDepositContract(address: String) : ERC20Contract(address) {

    val extraRewardsLength = constant<BigInteger>("extraRewardsLength", uint256())
    val stakingToken = constant<String>("stakingToken", address())
    val rewardToken = constant<String>("rewardToken", address())

    fun earnedFn(user: String): ContractCall {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    suspend fun extraRewards(): List<String> {
        return readMultiCall(
            (0 until extraRewardsLength.await().toInt()).map {
                createFunction(
                    "extraRewards",
                    listOf(it.toUint256()),
                    listOf(address())
                )
            }
        ).filter {
            it.success
        }.map {
            it.data[0].value as String
        }
    }

}