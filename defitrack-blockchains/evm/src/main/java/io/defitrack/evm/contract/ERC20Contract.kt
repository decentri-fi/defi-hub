package io.defitrack.evm.contract

import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

open class ERC20Contract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) :
    EvmContract(blockchainGateway,abi,  address) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        fun approveFunction(spender: String, amount: BigInteger): Function {
            return createFunction(
                "approve",
                listOf(spender.toAddress(), amount.toUint256()),
                listOf()
            )
        }

        fun fullApproveFunction(
            spender: String
        ): Function {
            return approveFunction(spender, BlockchainGateway.MAX_UINT256.value)
        }

        fun balanceOfFunction(address: String): Function {
            return createFunction(
                "balanceOf",
                inputs = listOf(address.toAddress()),
                outputs = listOf(
                    uint256()
                )
            )
        }
    }

    suspend fun readData(): List<List<Type<*>>> {
        return readMultiCall(
            listOf(
                createFunction("name", outputs = listOf(string())),
                createFunction("symbol", outputs = listOf(string())),
                createFunction("decimals", outputs = listOf(uint256())),
                createFunction("totalSupply", outputs = listOf(uint256())),
            )
        )
    }


    suspend fun allowance(owner: String, spender: String): BigInteger {
        return readWithoutAbi(
            "allowance",
            listOf(owner.toAddress(), spender.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }


    suspend fun balanceOf(address: String): BigInteger {
        return readWithoutAbi(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }


    suspend fun name(): String {
        return try {
            readSingle("name", string())
        } catch (ex: Exception) {
            logger.error("ERC20: Error reading name for token $address on ${blockchainGateway.network}")
            "unknown"
        }
    }

    suspend fun symbol(): String {
        return try {
            readSingle("symbol", string())
        } catch (ex: Exception) {
            "UNKWN"
        }
    }

    suspend fun decimals(): Int {
        return try {
            val d: BigInteger = readSingle("decimals", uint256())
            return d.toInt()
        } catch (ex: Exception) {
            18
        }
    }

    suspend fun totalSupply(): BigInteger {
        return try {
            readSingle("totalSupply", uint256())
        } catch (ex: Exception) {
            BigInteger.ZERO
        }
    }
}