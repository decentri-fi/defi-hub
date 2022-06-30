package io.defitrack.protocol.apeswap

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class ApeswapPolygonPoolingMarketProvider(
    private val apeswapPolygonGraphProvider: ApeswapPolygonGraphProvider,
    private val erC20Resource: ERC20Resource
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        apeswapPolygonGraphProvider.getPools().map { pool ->
            async {
                try {
                    val liquidityToken = erC20Resource.getTokenInformation(getNetwork(), pool.id)
                    PoolingMarket(
                        id = "ape-polygon-${pool.id}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = pool.id,
                        name = liquidityToken.name,
                        symbol = liquidityToken.symbol,
                        tokens = liquidityToken.underlyingTokens.map { it.toFungibleToken() },
                        tokenType = TokenType.APE,
                    )
                } catch (ex: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.APESWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}