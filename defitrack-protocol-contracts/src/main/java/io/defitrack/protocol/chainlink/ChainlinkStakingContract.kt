package io.defitrack.protocol.chainlink

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class ChainlinkStakingContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    fun getStake(user: String): ContractCall {
        return createFunction(
            "getStake",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun getBaseReward(user: String): ContractCall {
        return createFunction(
            method = "getBaseReward",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256())
        )
    }
}