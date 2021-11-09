package io.defitrack.protocol.convex

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CvxRewardPool(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
    val name: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
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

    fun stakingToken(): String {
        return read(
            "stakingToken",
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }

    fun rewardToken(): String {
        return read(
            "stakingToken",
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}