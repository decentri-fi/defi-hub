package io.defitrack.protocol.gains

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

class GainsNetworkStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun harvestDai(): ContractCall {
        return createFunction(
            "harvestDai",
            listOf(),
            listOf()
        )
    }

    fun pendingRewardsDai(address: String): ContractCall {
        return createFunction(
            "pendingRewardDai",
            listOf(address.toAddress()),
            listOf(uint128())
        )
    }

    val gns = constant<String>("gns", address())
    val dai = constant<String>("dai", address())

    fun totalGnsStaked(user: String): ContractCall {
        return createFunction(
            "totalGnsStaked",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }
}