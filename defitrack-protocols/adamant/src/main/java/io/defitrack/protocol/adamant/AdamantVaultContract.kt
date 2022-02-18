package io.defitrack.protocol.adamant

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class AdamantVaultContract(
    solidityBasedContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
    val lpAddress: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    val accRewardPerShare by lazy {
        read("accRewardPerShare")[0].value as BigInteger
    }

    val getRatio by lazy {
        read("getRatio")[0].value as BigInteger
    }

    val totalShares by lazy {
        read("totalShares")[0].value as BigInteger
    }

    val strategy by lazy {
        read("strategy")[0].value as String
    }

    val getRewardMultiplier by lazy {
        read("getRewardMultiplier")[0].value as BigInteger
    }

    val balance by lazy {
        read("balance")[0].value as BigInteger
    }

    fun getPendingReward(address: String): BigInteger {
        return read(
            "getPendingReward",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun getTokensStaked(address: String): BigInteger {
        return read(
            "getTokensStaked",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }
}