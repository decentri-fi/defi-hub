package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.evm.contract.toMultiCall
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class UniswapPositionsV3Contract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun getUserPositions(owner: String): List<UniswapPosition> {
        val balance = balanceOf(owner).toInt()
        val tokensOfOwner = getTokensOfOwner(balance, owner)
        return blockchainGateway.readMultiCall(
            tokensOfOwner.map {
                getPosition(it.toInt()).toMultiCall(address)
            }
        ).map {
            uniswapPosition(it)
        }
    }

    private suspend fun getTokensOfOwner(
        balance: Int,
        owner: String
    ): List<BigInteger> {
        val multicalls = (0 until balance).map { tokenOfOwnerByIndex(owner, it) }.map {
            it.toMultiCall(address)
        }
        return blockchainGateway.readMultiCall(
            multicalls
        ).map {
            it[0].value as BigInteger
        }
    }

    suspend fun tokenOfOwnerByIndex(owner: String, index: Int): Function {
        return createFunction(
            "tokenOfOwnerByIndex",
            listOf(owner.toAddress(), index.toBigInteger().toUint256()),
            listOf(TypeUtils.uint256())
        )
    }

    suspend fun balanceOf(owner: String): BigInteger {
        return readWithoutAbi(
            "balanceOf",
            listOf(owner.toAddress()),
            listOf(TypeUtils.uint256())
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
                TypeUtils.uint256(),
                TypeUtils.uint256(),
                TypeUtils.uint128(),
                TypeUtils.uint128()
            )
        )
    }

    suspend fun getAllPositions(): List<UniswapPosition> {
        println("getting indexes")
        val indexes = getIndexes()
        println("got indexes")
        val positions = blockchainGateway.readMultiCall(
            indexes.map {
                getPosition(it.toInt())
            }.map {
                MultiCallElement(
                    it, address
                )
            }
        ).map {
            uniswapPosition(it)
        }
        println("got positions")
        return positions
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

    fun ownerOfFn(tokenId: BigInteger): Function {
        return createFunction(
            "ownerOf",
            inputs = listOf(tokenId.toUint256()),
            outputs = listOf(TypeUtils.address())
        )
    }

    suspend fun getIndexes(): List<BigInteger> {
        val map = blockchainGateway.readMultiCall(
            (0 until totalSupply().toInt()).map {
                tokenByIndex(it)
            }.map {
                MultiCallElement(
                    it, address
                )
            }
        ).map {
            it[0].value as BigInteger
        }
        println("returning indexes")
        return map
    }

    suspend fun totalSupply(): BigInteger {
        return readSingle("totalSupply", TypeUtils.uint256())
    }

    fun tokenByIndex(index: Int): Function {
        return createFunction(
            "tokenByIndex",
            inputs = listOf(index.toBigInteger().toUint256()),
            outputs = listOf(TypeUtils.uint256())
        )
    }
}