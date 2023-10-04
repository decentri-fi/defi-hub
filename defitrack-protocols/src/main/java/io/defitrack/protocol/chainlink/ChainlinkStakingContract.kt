package io.defitrack.protocol.chainlink

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class ChainlinkStakingContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    fun getStake(user: String): Function {
        return createFunction(
            "getStake",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun getBaseReward(user: String): Function {
        return createFunction(
            method = "getBaseReward",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256())
        )
    }
}