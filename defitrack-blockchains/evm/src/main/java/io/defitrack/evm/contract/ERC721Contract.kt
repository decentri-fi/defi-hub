package io.defitrack.evm.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

open class ERC721Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) :
    EvmContract(blockchainGateway, address) {

    fun ownerOfFunction(tokenId: BigInteger): Function {
        return createFunction(
            "ownerOf",
            inputs = listOf(tokenId.toUint256()),
            outputs = listOf(TypeUtils.address())
        )
    }

    fun balanceOfMethod(address: String): Function {
        return createFunction(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                uint256()
            )
        )
    }

    suspend fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun name(): String {
        return try {
            read("name", outputs = listOf(string()))[0].value as String
        } catch (ex: Exception) {
            "unknown"
        }
    }

    suspend fun symbol(): String {
        return try {
            read("symbol", outputs = listOf(string()))[0].value as String
        } catch (ex: Exception) {
            "UNKWN"
        }
    }


    suspend fun totalSupply(): BigInteger {
        return try {
            read("totalSupply", outputs = listOf(uint256()))[0].value as BigInteger
        } catch (ex: Exception) {
            BigInteger.ZERO
        }
    }
}