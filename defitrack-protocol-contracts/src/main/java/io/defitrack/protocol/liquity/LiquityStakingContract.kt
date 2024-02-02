package io.defitrack.protocol.liquity

import arrow.core.nonEmptyListOf
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

class LiquityStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {


    fun stakes(user: String): ContractCall {
        return createFunction(
            "stakes",
            nonEmptyListOf(user.toAddress()),
            nonEmptyListOf(uint256())
        )
    }
}