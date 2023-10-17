package io.defitrack.protocol.mux

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class MuxYieldContract(blockchainGateway: BlockchainGateway, address: String) : EvmContract(
    blockchainGateway, address
) {
    val mlp = constant<String>("mlp", TypeUtils.address())
    val mux = constant<String>("mux", TypeUtils.address())
    val mcb = constant<String>("mcb", TypeUtils.address())

    fun claimableVestedTokenFromMlp(user: String): Function {
        return createFunction(
            "claimableVestedTokenFromMlp",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimableVestedTokenFromMux(user: String): Function {
        return createFunction(
            "claimableVestedTokenFromVe",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimFromMlpUnwrap(user: String): ContractCall {
        return createFunction(
            "claimFromMlpUnwrap",
            listOf(),
        ).toContractCall()
    }

    fun claimFromVeUnwrap(user: String): ContractCall {
        return createFunction(
            "claimFromVeUnwrap",
            listOf(),
        ).toContractCall()
    }
}