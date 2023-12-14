package io.defitrack.protocol.arpa

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

class ArpaStakingContract(blockchainGateway: BlockchainGateway, address: String) :
    EvmContract(blockchainGateway, address) {

    val arpaToken = constant<String>("getArpaToken", TypeUtils.address())

    fun claimReward(): ContractCall {
        return createFunction(
            "claimReward",
            emptyList(),
            emptyList()
        )
    }

    fun getStakeFn(staker: String): ContractCall {
        return createFunction(
            "getStake",
            listOf(staker.toAddress()),
            listOf(
                uint256(),
            )
        )
    }

    fun getBaseReward(staker: String): ContractCall {
        return createFunction(
            "getBaseReward",
            listOf(staker.toAddress()),
            listOf(uint256())
        )
    }
}