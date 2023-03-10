package io.defitrack.protocol.beefy.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class BeefyVaultContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
    val vaultId: String,
) :
    ERC20Contract(
        solidityBasedContractAccessor, abi, address
    ) {

    fun exitFunction(amount: BigInteger): Function {
        return createFunction(
            method = "withdraw",
            inputs = listOf(amount.toUint256())
        )
    }

    suspend fun balance(): BigInteger {
        return readWithAbi(
            "balance",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    fun depositAllFunction(): Function {
        return createFunctionWithAbi("depositAll", emptyList(), emptyList())
    }

    fun depositFunction(amount: BigInteger): Function {
        return createFunctionWithAbi("deposit", listOf(amount.toUint256()), emptyList())
    }

    suspend fun getPricePerFullShare(): BigInteger {
        return readWithAbi(
            "getPricePerFullShare",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun want(): String {
        var read = readWithoutAbi(
            "want",
            inputs = emptyList(),
            outputs = listOf(address())
        )
        if (read.isEmpty()) {
            read = readWithoutAbi(
                "wmatic",
                inputs = emptyList(),
                outputs = listOf(
                    address()
                )
            )
        }
        if (read.isEmpty()) {
            read = readWithAbi(
                "token",
                inputs = emptyList(),
                outputs = listOf(address())
            )
        }
        if (read.isEmpty()) {
            read = readWithAbi(
                "wbnb",
                inputs = emptyList(),
                outputs = listOf(address())
            )
        }
        return read[0].value as String
    }
}