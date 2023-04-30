package io.defitrack.protocol.camelot

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.CamelotService
import io.defitrack.protocol.algebra.AlgebraPosition
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component


@Component
class CamelotNFTV2PoolingMarketProvider(
    private val camelotService: CamelotService
) : PoolingMarketProvider() {


    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        val allPositions = camelotService.getAllPositions()
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

        val pool = camelotService.getPoolByPair(token0.address, token1.address)

        return create(
            tokens = listOf(token0.toFungibleToken(), token1.toFungibleToken()),
            identifier = pool,
            address = pool,
            name = "Camelot V3 ${token0.symbol}/${token1.symbol}",
            symbol = token0.symbol + "/" + token1.symbol,
            breakdown = emptyList(),
            erc20Compatible = false,
            tokenType = TokenType.ALGEBRA_NFT,
            totalSupply = it.liquidity,
            marketSize = getMarketSize(
                listOf(token0.toFungibleToken(), token1.toFungibleToken()),
                pool
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}