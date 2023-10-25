package io.defitrack.evm.contract

import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.evm.multicall.MultiCallResult
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

open class ERC20Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) :
    EvmContract(blockchainGateway, address) {

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

    suspend fun readAsMulticall(): ERC20MulticallResult {
        val result = readMultiCall(
            listOf(
                createFunction("name", outputs = listOf(string())),
                createFunction("symbol", outputs = listOf(string())),
                createFunction("decimals", outputs = listOf(uint256())),
                createFunction("totalSupply", outputs = listOf(uint256())),
            )
        )

        return ERC20MulticallResult(
            name = result[0],
            symbol = result[1],
            decimals = result[2],
            totalSupply = result[3],
        )
    }

    data class ERC20MulticallResult(
        val name: MultiCallResult,
        val symbol: MultiCallResult,
        val decimals: MultiCallResult,
        val totalSupply: MultiCallResult,
    )


    suspend fun readAllowance(owner: String, spender: String): BigInteger {
        return read(
            "allowance",
            listOf(owner.toAddress(), spender.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }


    suspend fun balanceOf(address: String): BigInteger {
        val retVal = read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )
        return if (retVal.isEmpty()) {
            logger.debug("Unable to fetch balance of on {} for {}", blockchainGateway.network, address)
            BigInteger.ZERO
        } else {
            retVal[0].value as BigInteger
        }
    }


    suspend fun readName(): String {
        return try {
            readSingle("name", string())
        } catch (ex: Exception) {
            logger.error("ERC20: Error reading name for token $address on ${blockchainGateway.network}")
            "unknown"
        }
    }

    suspend fun readSymbol(): String {
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

    suspend fun totalSupply(): Refreshable<BigInteger> {
       return refreshable {
            try {
                readSingle("totalSupply", uint256())
            } catch (ex: Exception) {
                BigInteger.ZERO
            }
        }
    }
}