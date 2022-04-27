package io.defitrack.protocol.quickswap

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class QuickswapDualRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : EvmContract(
    solidityBasedContractAccessor,
    abi, address
) {

    val totalSupply by lazy {
        read("totalSupply")[0].value as BigInteger
    }

    val rewardsTokenAddressA by lazy {
        read(
            "rewardsTokenA",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }


    val rewardsTokenAddressB by lazy {
        read(
            "rewardsTokenB",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val stakingTokenAddress by lazy {
        read(
            method = "stakingToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val rewardRateA by lazy {
        read(
            method = "rewardRateA",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }


    val rewardRateB by lazy {
        read(
            method = "rewardRateB",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun earnedA(address: String): BigInteger {
        return read(
            "earnedA",
            listOf(address.toAddress()),
            listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun earnedB(address: String): BigInteger {
        return read(
            "earnedB",
            listOf(address.toAddress()),
            listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}