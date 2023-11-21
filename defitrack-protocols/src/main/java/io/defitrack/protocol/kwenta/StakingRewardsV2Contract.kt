package io.defitrack.protocol.kwenta

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class StakingRewardsV2Contract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {


    fun earnedfn(user: String): Function {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    val kwenta = constant<String>("kwenta", address())

    fun claimFn(): MutableFunction {
        return createFunction("compound").toMutableFunction()
    }
}