package io.defitrack.protocol.mstable.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MStablePolygonBoostedSavingsVaultContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : EvmContract(ethereumContractAccessor, abi, address) {

    val symbol: String by lazy {
        try {
            readWithAbi(
                "symbol"
            )[0].value as String
        } catch (ex: Exception) {
            """v-unkknown"""
        }
    }

    val decimals: Int by lazy {
        try {
            (readWithAbi(
                "decimals"
            )[0].value as BigInteger).toInt()
        } catch (ex: Exception) {
            18
        }
    }

    val name: String by lazy {
        try {
            readWithAbi(
                "name"
            )[0].value as String
        } catch (ex: Exception) {
            """unknown Vault"""
        }
    }

    fun rawBalanceOf(address: String): BigInteger {
        return readWithAbi(
            "rawBalanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }


    fun unclaimedRewards(address: String): BigInteger {
        return readWithAbi(
            "unclaimedRewards",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val rewardsToken by lazy {
        (readWithAbi(
            "rewardsToken"
        )[0].value as String)
    }

    val stakingToken by lazy {
        (readWithAbi(
            "stakingToken"
        )[0].value as String)
    }
}