package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurvePoolGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import io.defitrack.token.MarketSizeService
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal

class CurvePoolingMarketProvider(
    private val curvePoolGraphProvider: CurvePoolGraphProvider,
    private val marketSizeService: MarketSizeService,
    erc20Resource: ERC20Resource,
) : PoolingMarketProvider(erc20Resource) {

    override suspend fun fetchMarkets(): List<PoolingMarket> =
        coroutineScope {
            curvePoolGraphProvider.getPools().map { pool ->
                async {
                    try {
                        val tokens = pool.coins.map { coin ->
                            getToken(coin)
                        }.map { it.toFungibleToken() }

                        val lpToken = getToken(pool.address)

                        PoolingMarket(
                            id = "curve-${getNetwork().slug}-${pool.lpToken}",
                            network = getNetwork(),
                            protocol = getProtocol(),
                            address = pool.address,
                            name = lpToken.name,
                            symbol = lpToken.symbol,
                            tokens = tokens,
                            apr = BigDecimal.ZERO,
                            marketSize = calculateMarketSize(tokens, lpToken.address),
                            tokenType = TokenType.CURVE,
                            positionFetcher = defaultBalanceFetcher(lpToken.address)
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error("Unable to import curve pool ${pool.address}", ex.message)
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
        return curvePoolGraphProvider.network
    }
}