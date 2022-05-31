package io.defitrack.protocol.adamant

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class AdamantVaultContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : EvmContract(solidityBasedContractAccessor, abi, address) {

    val accRewardPerShare by lazy {
        readWithAbi("accRewardPerShare")[0].value as BigInteger
    }

    val getRatio by lazy {
        readWithAbi("getRatio")[0].value as BigInteger
    }

    val totalShares by lazy {
        readWithAbi("totalShares")[0].value as BigInteger
    }

    val strategy by lazy {
        readWithAbi("strategy")[0].value as String
    }

    val getRewardMultiplier by lazy {
        readWithAbi("getRewardMultiplier")[0].value as BigInteger
    }

    val balance by lazy {
        readWithAbi("balance")[0].value as BigInteger
    }

    fun balanceOfMethod(address: String): Function {
        return createFunctionWithAbi(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                BlockchainGateway.uint256()
            )
        )
    }

    val token by lazy {
        readWithAbi("token")[0].value as String
    }

    fun getPendingReward(address: String): BigInteger {
        return readWithAbi(
            "getPendingReward",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun getTokensStaked(address: String): BigInteger {
        return readWithAbi(
            "getTokensStaked",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun getClaimFunction(): Function {
        return createFunctionWithAbi(
            "claim",
            emptyList(),
            emptyList()
        )
    }
}