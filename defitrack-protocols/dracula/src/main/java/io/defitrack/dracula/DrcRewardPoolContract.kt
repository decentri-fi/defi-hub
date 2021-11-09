package io.defitrack.dracula

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class DrcRewardPoolContract(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
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

    val want by lazy {
        val wantAddress = read(
            "stakingToken",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as String
    }

    val rewardToken by lazy {
        val wantAddress = read(
            "rewardToken",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as String
    }
}