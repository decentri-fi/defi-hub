package io.defitrack.protocol.quickswap.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class QuickswapDualRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor,
    abi, address
) {

    suspend fun rewardsTokenAddressA(): String {
        return read("rewardsTokenA")
    }


    suspend fun rewardsTokenAddressB(): String {
        return read(
            "rewardsTokenB"
        )
    }

    suspend fun stakingTokenAddress(): String {
        return read("stakingToken");
    }

    suspend fun rewardRateA(): BigInteger {
        return readWithAbi(
            method = "rewardRateA",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }


    suspend fun rewardRateB(): BigInteger {
        return readWithAbi(
            method = "rewardRateB",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    suspend fun earnedA(address: String): BigInteger {
        return readWithAbi(
            "earnedA",
            listOf(address.toAddress()),
            listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    suspend fun earnedB(address: String): BigInteger {
        return readWithAbi(
            "earnedB",
            listOf(address.toAddress()),
            listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}