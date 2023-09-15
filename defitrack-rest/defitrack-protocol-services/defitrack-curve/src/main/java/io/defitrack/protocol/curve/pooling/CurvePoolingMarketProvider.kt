package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurvePoolGraphProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

abstract class CurvePoolingMarketProvider(
    private val curvePoolGraphProvider: CurvePoolGraphProvider
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        curvePoolGraphProvider.getPools().forEach { pool ->
            launch {
                throttled {
                    try {
                        val tokens = pool.coins.map { coin ->
                            getToken(coin)
                        }.map { it.toFungibleToken() }

                        val lpToken = getToken(pool.address)

                        send(
                            create(
                                identifier = pool.lpToken,
                                address = pool.address,
                                name = lpToken.name,
                                symbol = lpToken.symbol,
                                tokens = tokens,
                                apr = BigDecimal.ZERO,
                                marketSize = Refreshable.refreshable {
                                    marketSizeService.getMarketSize(tokens, lpToken.address, getNetwork())
                                },
                                tokenType = TokenType.CURVE,
                                positionFetcher = if (lpToken.name == "unknown") null else defaultPositionFetcher(
                                    lpToken.address
                                ),
                                totalSupply = Refreshable.refreshable(lpToken.totalDecimalSupply()) {
                                    getToken(pool.address).totalDecimalSupply()
                                }
                            )
                        )
                    } catch (ex: Exception) {
                        logger.error("Unable to import curve pool ${pool.address}", ex)
                    }
                }
            }
        }
    }

    override fun getNetwork(): Network {
        return curvePoolGraphProvider.network
    }
}