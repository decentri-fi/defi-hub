package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.datatypes.Function

class BalancerGaugeZkEvmContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : BalancerGaugeContract(blockchainGateway, address) {

    override fun getClaimableRewardFunction(address: String, token: String): Function {
        return Function(
            "claimable_reward",
            listOf(
                address.toAddress(),
                token.toAddress()
            ),
            listOf(uint256())
        )
    }
}