package io.defitrack.protocol.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class HopStakingRewards(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : ERC20Contract(blockchainGateway, abi, address) {

    val rewardPerToken by lazy {
        read("rewardPerToken")[0].value as BigInteger
    }


    val rewardRate by lazy {
        read("rewardRate")[0].value as BigInteger
    }

    val stakingToken by lazy {
        read("stakingToken")[0].value as String
    }

    val rewardsToken by lazy {
        read("rewardsToken")[0].value as String
    }

    fun earned(address: String): BigInteger {
        return read(
            "earned",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }
}