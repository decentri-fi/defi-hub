package io.defitrack.protocol.synthetix

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class LiquidatorRewardsContract(
    address: String
) : EvmContract(address) {

    fun earnedFn(user: String): ContractCall {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun getRewardFn(): (String) -> ContractCall {
        return {
            createFunction(
                "getReward",
                listOf(it.toAddress()),
                emptyList()
            )
        }
    }
}