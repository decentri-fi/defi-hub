package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class UniswapPositionsV3Contract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    suspend fun getUserPositions(owner: String): List<UniswapPosition> {
        val balance = balanceOf(owner).toInt()
        val tokensOfOwner = getTokensOfOwner(balance, owner)
        return readMultiCall(
            tokensOfOwner.map {
                getPosition(it.toInt())
            }
        ).map {
            uniswapPosition(it.data)
        }
    }

    private suspend fun getTokensOfOwner(
        balance: Int,
        owner: String
    ): List<BigInteger> {
        val functions = (0 until balance).map { tokenOfOwnerByIndex(owner, it) }
        return readMultiCall(functions).map {
            it.data[0].value as BigInteger
        }
    }

    fun tokenOfOwnerByIndex(owner: String, index: Int): Function {
        return createFunction(
            "tokenOfOwnerByIndex",
            listOf(owner.toAddress(), index.toBigInteger().toUint256()),
            listOf(uint256())
        )
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
                TypeUtils.uint88(),
                TypeUtils.address(),
                TypeUtils.address(),
                TypeUtils.address(),
                TypeUtils.int24(),
                TypeUtils.int24(),
                TypeUtils.uint128(),
                uint256(),
                uint256(),
                TypeUtils.uint128(),
                TypeUtils.uint128()
            )
        )
    }

    private fun uniswapPosition(it: List<Type<*>>) = UniswapPosition(
        it[2].value as String,
        it[3].value as String,
        it[4].value as BigInteger,
        it[7].value as BigInteger,
        it[5].value as BigInteger,
        it[6].value as BigInteger,
        it[8].value as BigInteger,
        it[9].value as BigInteger,
    )

    suspend fun totalSupply(): BigInteger {
        return readSingle("totalSupply", uint256())
    }
}