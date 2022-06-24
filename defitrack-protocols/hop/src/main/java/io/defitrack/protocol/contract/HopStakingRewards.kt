package io.defitrack.protocol.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.abi.TypeUtils.Companion.toAddress
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
        readWithAbi("rewardPerToken")[0].value as BigInteger
    }


    val rewardRate by lazy {
        readWithAbi("rewardRate")[0].value as BigInteger
    }

    val stakingToken by lazy {
        readWithAbi("stakingToken")[0].value as String
    }

    val rewardsToken by lazy {
        readWithAbi("rewardsToken")[0].value as String
    }

    fun earned(address: String): BigInteger {
        return readWithAbi(
            "earned",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }
}