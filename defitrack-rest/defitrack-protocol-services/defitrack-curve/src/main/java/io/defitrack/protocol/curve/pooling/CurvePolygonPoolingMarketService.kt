package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurvePolygonPoolGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import io.defitrack.token.MarketSizeService
import io.defitrack.token.TokenType
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CurvePolygonPoolingMarketService(
    private val curvePolygonPoolGraphProvider: CurvePolygonPoolGraphProvider,
    private val erc20Resource: ERC20Resource,
    private val marketSizeService: MarketSizeService,
) : PoolingMarketProvider() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> = withContext(Dispatchers.IO.limitedParallelism(10)) {
         curvePolygonPoolGraphProvider.getPools().map { pool ->
            async {
                try {
                    val tokens = pool.coins.map { coin ->
                        erc20Resource.getTokenInformation(getNetwork(), coin)
                    }.map { it.toFungibleToken() }

                    val lpToken = erc20Resource.getTokenInformation(getNetwork(), pool.address)

                    PoolingMarketElement(
                        id = "curve-polygon-${pool.lpToken}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = pool.address,
                        name = lpToken.name,
                        symbol = lpToken.symbol,
                        tokens = tokens,
                        apr = BigDecimal.ZERO,
                        marketSize = calculateMarketSize(tokens, lpToken.address),
                        tokenType = TokenType.CURVE
                    )
                } catch (ex: Exception) {
                    logger.error("Unable to import curve pool ${pool.address}")
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun calculateMarketSize(tokens: List<FungibleToken>, holder: String): BigDecimal {
        return tokens.sumOf { token ->
            marketSizeService.getMarketSize(
                token, holder, getNetwork()
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}