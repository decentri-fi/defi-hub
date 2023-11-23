package io.defitrack.protocol.algebra

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.int24
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint88
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class AlgebraPositionsV2Contract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
) {
    fun getPositionFunction(tokenId: Int): ContractCall {
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
                getPositionFunction(it.toInt())
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

    suspend fun getIndexes(): List<BigInteger> {
        return readMultiCall(
            (0 until totalSupply().get().toInt()).map {
                tokenByIndex(it)
            }
        ).map {
            it.data[0].value as BigInteger
        }
    }

    fun tokenByIndex(index: Int): ContractCall {
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