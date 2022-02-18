package io.defitrack.protocol.mstable

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MStableEthereumBoostedSavingsVaultContract(
    ethereumContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
) : EvmContract(ethereumContractAccessor, abi, address) {

    val symbol: String by lazy {
        try {
            read(
                "symbol"
            )[0].value as String
        } catch (ex: Exception) {
            """unknown"""
        }
    }

    val decimals: Int by lazy {
        try {
            (read(
                "decimals"
            )[0].value as BigInteger).toInt()
        } catch (ex: Exception) {
            18
        }
    }

    val name: String by lazy {
        try {
            read(
                "name"
            )[0].value as String
        } catch (ex: Exception) {
            """unknown vault"""
        }
    }

    fun rawBalanceOf(address: String): BigInteger {
        return read(
            "rawBalanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }


    fun unclaimedRewards(address: String): BigInteger {
        return read(
            "unclaimedRewards",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val rewardsToken by lazy {
        (read(
            "rewardsToken"
        )[0].value as String)
    }

    val stakingToken by lazy {
        (read(
            "stakingToken"
        )[0].value as String)
    }
}