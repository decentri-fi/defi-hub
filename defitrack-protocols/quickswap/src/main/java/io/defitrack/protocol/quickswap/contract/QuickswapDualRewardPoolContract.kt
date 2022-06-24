package io.defitrack.protocol.quickswap.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
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

    val rewardsTokenAddressA by lazy {
        readWithAbi(
            "rewardsTokenA",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }


    val rewardsTokenAddressB by lazy {
        readWithAbi(
            "rewardsTokenB",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val stakingTokenAddress by lazy {
        readWithAbi(
            method = "stakingToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val rewardRateA by lazy {
        readWithAbi(
            method = "rewardRateA",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }


    val rewardRateB by lazy {
        readWithAbi(
            method = "rewardRateB",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun earnedA(address: String): BigInteger {
        return readWithAbi(
            "earnedA",
            listOf(address.toAddress()),
            listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun earnedB(address: String): BigInteger {
        return readWithAbi(
            "earnedB",
            listOf(address.toAddress()),
            listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}