package io.defitrack.protocol.plutusdao

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract

class StakedPLSContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {


    fun claimable(user: String): ContractCall {
        return createFunction(
            "claimable",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun stakedAmounts(user: String): ContractCall {
        return createFunction(
            "stakedAmounts",
            user.toAddress().nel(),
            uint256().nel()
        )
    }
}