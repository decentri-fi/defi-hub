package io.defitrack.protocol.gains

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class GainsNetworkStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun harvestDai(): Function {
        return createFunction(
            "harvestDai",
            listOf(),
            listOf()
        )
    }

    fun pendingRewardsDai(address: String): Function {
        return createFunction(
            "pendingRewardDai",
            listOf(address.toAddress()),
            listOf(uint128())
        )
    }

    val gns = constant<String>("gns", address())
    val dai = constant<String>("dai", address())

    fun totalGnsStaked(address: String): Function {
        return createFunction(
            "totalGnsStaked",
            listOf(address.toAddress()),
            listOf(uint256())
        )
    }
}