package io.defitrack.evm.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
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

    suspend fun allowance(owner: String, spender: String): BigInteger {
        return readWithAbi(
            "allowance",
            listOf(owner.toAddress(), spender.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
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

    fun approveFunction(spender: String, amount: BigInteger): Function {
        return createFunctionWithAbi(
            "approve",
            listOf(spender.toAddress(), amount.toUint256()),
            listOf()
        )
    }

    suspend fun balanceOf(address: String): BigInteger {
        return readWithAbi(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    suspend fun name(): String {
        return try {
            read("name")
        } catch (ex: Exception) {
            "unknown"
        }
    }

    suspend fun symbol(): String {
        return try {
            read("symbol")
        } catch (ex: Exception) {
            "UNKWN"
        }
    }

    suspend fun decimals(): Int {
        return try {
            val d: BigInteger = read("decimals")
            return d.toInt()
        } catch (ex: Exception) {
            18
        }
    }

    suspend fun totalSupply(): BigInteger {
        return try {
            return read("totalSupply")
        } catch (ex: Exception) {
            return BigInteger.ZERO
        }
    }
}