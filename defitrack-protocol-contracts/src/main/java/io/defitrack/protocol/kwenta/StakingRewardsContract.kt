package io.defitrack.protocol.kwenta

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class StakingRewardsContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(blockchainGateway, address) {


    fun earnedfn(user: String): ContractCall {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    val kwenta = constant<String>("token", address())

    fun claimFn(): ContractCall {
        return createFunction("getReward")
    }
}