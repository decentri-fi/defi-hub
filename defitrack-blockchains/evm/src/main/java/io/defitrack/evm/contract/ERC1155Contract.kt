package io.defitrack.evm.contract

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import java.math.BigInteger

open class ERC1155Contract(
    blockchainGateway: BlockchainGateway,
    address: String
) :
    EvmContract(blockchainGateway, address) {

    suspend fun balanceOf(user: String, tokenId: BigInteger): BigInteger {
        return readSingle(
            method = "balanceOf",
            inputs = listOf(
                user.toAddress(),
                tokenId.toUint256()
            ),
            output = uint256()
        )
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