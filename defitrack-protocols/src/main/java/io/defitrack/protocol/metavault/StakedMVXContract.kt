package io.defitrack.protocol.metavault

import io.defitrack.abi.TypeUtils.Companion
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.FarmingContract
import org.apache.commons.lang3.reflect.TypeUtils
import org.web3j.abi.datatypes.Function

class StakedMVXContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway,
    address,
) {


    val rewardtoken = constant<String>("rewardToken", address())

    fun claimableFn(user: String): Function {
        return createFunction(
            "claimable",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun balanceOfFn(user: String): Function {
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
        ).toContractCall()
    }
}