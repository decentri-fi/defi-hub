package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class UniswapPositionsV2Contract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, "", address
) {


    suspend fun getUserPositions(owner: String): List<UniswapPosition> {
        val balance = balanceOf(owner).toInt()
        return blockchainGateway.readMultiCall(
            (0 until balance).map { tokenOfOwnerByIndex(owner, it) }.map {
                getPosition(it.toInt())
            }.map {
                MultiCallElement(it, address)
            }
        ).map {
            algebraPosition(it)
        }
    }

    suspend fun tokenOfOwnerByIndex(owner: String, index: Int): BigInteger {
        return readWithoutAbi(
            "tokenOfOwnerByIndex",
            listOf(owner.toAddress(), index.toBigInteger().toUint256()),
            listOf(TypeUtils.uint256())
        )[0].value as BigInteger
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
        val indexes = getIndexes()
        return blockchainGateway.readMultiCall(
            indexes.map {
                getPosition(it.toInt())
            }.map {
                MultiCallElement(
                    it, address
                )
            }
        ).map {
            algebraPosition(it)
        }
    }

    private fun algebraPosition(it: List<Type<*>>) = UniswapPosition(
        it[2].value as String,
        it[3].value as String,
        it[7].value as BigInteger,
    )

    fun ownerOfFn(tokenId: BigInteger): Function {
        return createFunction(
            "ownerOf",
            inputs = listOf(tokenId.toUint256()),
            outputs = listOf(TypeUtils.address())
        )
    }

    suspend fun getIndexes(): List<BigInteger> {
        return blockchainGateway.readMultiCall(
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