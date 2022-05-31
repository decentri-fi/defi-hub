package io.defitrack.evm.contract

import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway.Companion.uint256
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

open class ERC20Contract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) :
    EvmContract(blockchainGateway, abi, address) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun allowance(owner: String, spender: String): BigInteger {
        return readWithAbi(
            "allowance",
            listOf(owner.toAddress(), spender.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }

    fun approveFunction(spender: String, amount: BigInteger): Function {
        return createFunctionWithAbi(
            "approve",
            listOf(spender.toAddress(), amount.toUint256()),
            listOf()
        )
    }


    fun balanceOfMethod(address: String): Function {
        return createFunctionWithAbi(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                uint256()
            )
        )
    }

    fun balanceOf(address: String): BigInteger {
        return readWithAbi(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val name by lazy {
        try {
            readWithAbi("name")[0].value as String
        } catch (ex: Exception) {
            "unknown"
        }
    }

    val symbol by lazy {
        val read = readWithAbi("symbol")
        if (read.isEmpty()) {
            "unknown"
        } else {
            read[0].value as String
        }
    }

    val decimals by lazy {
        val read = readWithAbi("decimals")
        if (read.isEmpty()) {
            18
        } else {
            (read[0].value as BigInteger).toInt()
        }
    }

    val totalSupply by lazy {
        val read = readWithAbi("totalSupply")
        if (read.isEmpty()) {
            18
        } else {
            (read[0].value as BigInteger).toInt()
        }
    }
}