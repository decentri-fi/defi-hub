package io.defitrack.protocol.camelot

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigInteger

private const val CAMELOT_NFT = "0xacdcc3c6a2339d08e0ac9f694e4de7c52f890db3"

@Component
class CamelotNFTV2PoolingMarketProvider : PoolingMarketProvider() {

    val algebraPositionsContract by lazy {
        AlgebraPositionsV2Contract(
            blockchainGateway = getBlockchainGateway(),
            address = CAMELOT_NFT
        )
    }

    val algebraFactoryContract by lazy {
        AlgebraFactoryContract(
            getBlockchainGateway(),
            "0xd490f2f6990c0291597fd1247651b4e0dcf684dd"
        )
    }

    suspend fun getAllPositions(): List<AlgebraPosition> {
       return algebraPositionsContract.getAllPositions()
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        val allPositions = getAllPositions()
        allPositions.distinctBy {
            listOf(it.token0, it.token1).sorted().joinToString("-")
        }.map {
            async {
                toMarket(it)
            }

        }.awaitAll()
    }

    suspend fun toMarket(it: AlgebraPosition): PoolingMarket {
        val token0 = getToken(it.token0)
        val token1 = getToken(it.token1)

        val pool = algebraFactoryContract.getPoolByPair(token0.address, token1.address)

        return create(
            tokens = listOf(token0.toFungibleToken(), token1.toFungibleToken()),
            identifier = pool,
            address = CAMELOT_NFT,
            name = "Camelot V3 ${token0.symbol}/${token1.symbol}",
            symbol = token0.symbol + "/" + token1.symbol,
            breakdown = emptyList(),
            erc20Compatible = false,
            tokenType = TokenType.ALGEBRA_NFT,
            totalSupply = BigInteger.ZERO,
            marketSize = getMarketSize(
                listOf(token0.toFungibleToken(), token1.toFungibleToken()),
                pool
            )
        )
    }

    private fun getPositions(): Any {
        TODO("Not yet implemented")
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }


}