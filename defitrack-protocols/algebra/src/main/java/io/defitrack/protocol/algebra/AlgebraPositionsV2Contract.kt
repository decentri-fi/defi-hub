package io.defitrack.protocol.algebra

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.int24
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint88
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class AlgebraPositionsV2Contract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun getUserPositions(owner: String): List<AlgebraPosition> {
        val balance = balanceOf(owner).toInt()
        return readMultiCall(
            (0 until balance).map { tokenOfOwnerByIndex(owner, it) }.map {
                getPosition(it.toInt())
            }
        ).map {
            algebraPosition(it.data)
        }
    }

    suspend fun tokenOfOwnerByIndex(owner: String, index: Int): BigInteger {
        return read(
            "tokenOfOwnerByIndex",
            listOf(owner.toAddress(), index.toBigInteger().toUint256()),
            listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun balanceOf(owner: String): BigInteger {
        return read(
            "balanceOf",
            listOf(owner.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }

    fun getPosition(tokenId: Int): Function {
        return createFunction(
            method = "positions",
            inputs = listOf(tokenId.toBigInteger().toUint256()),
            outputs = listOf(
                uint88(),
                address(),
                address(),
                address(),
                int24(),
                int24(),
                uint128(),
                uint256(),
                uint256(),
                uint128(),
                uint128()
            )
        )
    }

    suspend fun getAllPositions(): List<AlgebraPosition> {
        val indexes = getIndexes()
        return readMultiCall(
            indexes.map {
                getPosition(it.toInt())
            }
        ).map {
            algebraPosition(it.data)
        }
    }

    private fun algebraPosition(it: List<Type<*>>) = AlgebraPosition(
        it[2].value as String,
        it[3].value as String,
        it[6].value as BigInteger,
        it[9].value as BigInteger,
        it[10].value as BigInteger
    )

    fun ownerOfFn(tokenId: BigInteger): Function {
        return createFunction(
            "ownerOf",
            inputs = listOf(tokenId.toUint256()),
            outputs = listOf(address())
        )
    }

    suspend fun getIndexes(): List<BigInteger> {
        return readMultiCall(
            (0 until totalSupply().toInt()).map {
                tokenByIndex(it)
            }
        ).map {
            it.data[0].value as BigInteger
        }
    }

    suspend fun totalSupply(): BigInteger {
        return readSingle("totalSupply", uint256())
    }

    fun tokenByIndex(index: Int): Function {
        return createFunction(
            "tokenByIndex",
            inputs = listOf(index.toBigInteger().toUint256()),
            outputs = listOf(uint256())
        )
    }
}

data class AlgebraPosition(
    val token0: String,
    val token1: String,
    val liquidity: BigInteger,
    val tokensOwed0: BigInteger,
    val tokensOwed1: BigInteger
)