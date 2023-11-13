package io.defitrack.protocol.plutusdao

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function

class StakedPLSContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {


    fun claimable(user: String): Function {
        return createFunction(
            "claimable",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun stakedAmounts(user: String): Function {
        return createFunction(
            "stakedAmounts",
            user.toAddress().nel(),
            uint256().nel()
        )
    }
}