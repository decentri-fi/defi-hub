package io.defitrack.protocol.balancer.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class BalancerGaugeContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : ERC20Contract(blockchainGateway, abi, address) {

    fun getClaimableReward(address: String, token: String): BigInteger {
        return read(
            "claimable_reward",
            listOf(
                address.toAddress(),
                token.toAddress()
            ),
            listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun getRewardToken(index: Int): String {
        return read(
            "reward_tokens",
            listOf(index.toBigInteger().toUint256()),
            listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }
}