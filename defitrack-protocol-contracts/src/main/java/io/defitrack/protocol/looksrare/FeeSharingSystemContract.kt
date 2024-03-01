package io.defitrack.protocol.looksrare

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class FeeSharingSystemContract( address: String
) : EvmContract( address
) {

    val looksRareToken = constant<String>("looksRareToken", address())
    val rewardToken = constant<String>("rewardToken", address())

    fun userInfoFn(user: String): ContractCall {
        return createFunction(
            "userInfo",
            listOf(
                user.toAddress()
            ),
            listOf(
                uint256(),
                uint256(),
                uint256()
            )
        )
    }

    fun calculateSharesValueInLooks(user: String): ContractCall {
        return createFunction(
            "calculateSharesValueInLOOKS",
            listOf(
                user.toAddress()
            ),
            listOf(
                uint256(),
            )
        )
    }

    fun calculatePendingRewards(user: String): ContractCall {
        return createFunction(
            "calculatePendingRewards",
            listOf(
                user.toAddress()
            ),
            listOf(
                uint256(),
            )
        )
    }

    fun harvest(): ContractCall {
        return createFunction("harvest")
    }
}