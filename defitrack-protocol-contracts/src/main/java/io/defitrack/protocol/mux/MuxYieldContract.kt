package io.defitrack.protocol.mux

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

class MuxYieldContract(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {
    val mlp = constant<String>("mlp", TypeUtils.address())
    val mux = constant<String>("mux", TypeUtils.address())
    val mcb = constant<String>("mcb", TypeUtils.address())

    fun claimableVestedTokenFromMlp(user: String): ContractCall {
        return createFunction(
            "claimableVestedTokenFromMlp",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimableVestedTokenFromMux(user: String): ContractCall {
        return createFunction(
            "claimableVestedTokenFromVe",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimFromMlpUnwrap(): ContractCall {
        return createFunction(
            "claimFromMlpUnwrap",
            listOf(),
        )
    }

    fun claimFromVeUnwrap(): ContractCall {
        return createFunction("claimFromVeUnwrap")
    }
}