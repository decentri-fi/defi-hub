package io.defitrack.protocol.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class HopStakingReward(
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

    val periodFinish by lazy {
        read(
            "periodFinish",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val rewardsTokenAddress by lazy {
        read(
            "rewardsToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val stakingTokenAddress by lazy {
        read(
            method = "stakingToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    val rewardRate by lazy {
        read(
            method = "rewardRate",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun earned(address: String): BigInteger {
        return read(
            "earned",
            listOf(address.toAddress()),
            listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}