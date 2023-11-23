package io.defitrack.protocol.metavault

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class StakedMVXContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway,
    address,
) {


    val rewardtoken = constant<String>("rewardToken", address())

    fun claimableFn(user: String): ContractCall {
        return createFunction(
            "claimable",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun balanceOfFn(user: String): ContractCall {
        return createFunction(
            "stakedAmounts",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimFn(user: String): ContractCall {
        return createFunction(
            "claim",
            listOf(user.toAddress()),
        )
    }
}