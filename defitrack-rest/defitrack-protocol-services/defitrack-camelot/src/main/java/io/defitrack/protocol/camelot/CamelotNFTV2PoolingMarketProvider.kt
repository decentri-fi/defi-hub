package io.defitrack.protocol.camelot

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.CamelotService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.algebra.AlgebraPoolContract
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

    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    suspend fun toMarket(it: AlgebraPosition): PoolingMarket {
        val token0 = getToken(it.token0)
        val token1 = getToken(it.token1)

        val pool = AlgebraPoolContract(
            getBlockchainGateway(),
            camelotService.getPoolByPair(token0.address, token1.address)
        )

        return create(
            tokens = listOf(token0.toFungibleToken(), token1.toFungibleToken()),
            identifier = pool.address,
            address = pool.address,
            name = "Camelot V3 ${token0.symbol}/${token1.symbol}",
            symbol = token0.symbol + "/" + token1.symbol,
            breakdown = emptyList(),
            erc20Compatible = false,
            tokenType = TokenType.ALGEBRA_NFT,
            totalSupply = Refreshable.refreshable {
                pool.liquidity().asEth()
            },
            marketSize = Refreshable.refreshable {
                getMarketSize(
                    listOf(token0.toFungibleToken(), token1.toFungibleToken()),
                    pool.address
                )
            }
        )
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}