package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function

class BalancerL2PseudoMinterContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(blockchainGateway, address) {

    val bal = constant<String>("getBalancerToken", TypeUtils.address())

    fun mintedFn(user: String, gauge: String): ContractCall {
        return createFunction(
            "minted",
            listOf(user.toAddress(), gauge.toAddress()),
            listOf(uint256())
        )
    }
}