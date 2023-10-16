package io.defitrack.protocol.kwenta

import io.defitrack.abi.TypeUtils.Companion
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.springframework.util.TypeUtils
import org.web3j.abi.datatypes.Function

class StakingRewardsContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {


    fun earnedfn(user: String): Function {
        return createFunction(
            "earned",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    val kwenta = constant<String>("token", address())

    fun claimFn(): ContractCall {
        return createFunction("getReward").toContractCall()
    }
}