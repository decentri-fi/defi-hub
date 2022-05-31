package io.defitrack.protocol.convex.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.generated.Uint112
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint32
import java.math.BigInteger

class CvxLockerContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String,
    val name: String
) : EvmContract(blockchainGateway, abi, address) {

    fun lockedBalances(address: String) {
        val retVal = readWithAbi(
            method = "lockedBalances",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(DynamicBytes::class.java)
            )
        )
    }

    fun rewardToken(): String {
        return readWithAbi(
            method = "rewardTokens",
            inputs = listOf(BigInteger.ZERO.toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
            )
        )[0].value as String
    }

    fun stakingToken(): String {
        return readWithAbi(
            method = "stakingToken",
            inputs = emptyList(),
            outputs = listOf(
                TypeReference.create(Address::class.java),
            )
        )[0].value as String
    }

    fun balances(address: String): BigInteger {
        return readWithAbi(
            method = "balances",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint112::class.java),
                TypeReference.create(Uint112::class.java),
                TypeReference.create(Uint32::class.java)
            )
        )[0].value as BigInteger
    }
}