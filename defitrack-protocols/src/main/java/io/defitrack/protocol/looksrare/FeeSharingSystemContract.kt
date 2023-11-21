package io.defitrack.protocol.looksrare

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class FeeSharingSystemContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    val looksRareToken = constant<String>("looksRareToken", TypeUtils.address())
    val rewardToken = constant<String>("rewardToken", TypeUtils.address())

    fun userInfoFn(user: String): Function {
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

    fun calculateSharesValueInLooks(user: String): Function {
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

    fun calculatePendingRewards(user: String): Function {
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

    fun harvest(user: String): MutableFunction {
        return createFunction(
            "harvest",
            listOf(
            ),
            listOf(
            )
        ).toMutableFunction()
    }
}