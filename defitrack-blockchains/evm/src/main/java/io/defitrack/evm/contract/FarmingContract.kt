package io.defitrack.evm.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256

open class FarmingContract(
    blockchainGateway: BlockchainGateway,
    address: String,
    stakedTokenName: String = "stakedToken",
    rewardTokenName: String = "rewardToken",
    val rewardFunctionName: String = "earned",
    val claimFunctionName: String = "getReward"
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    val stakedToken = constant<String>(stakedTokenName, TypeUtils.address())
    val rewardToken = constant<String>(rewardTokenName, TypeUtils.address())

    open fun getRewardFn(user: String): ContractCall {
        return createFunction(
            rewardFunctionName,
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    open fun claimFn(user: String): ContractCall {
        return createFunction(
            claimFunctionName,
        )
    }
}