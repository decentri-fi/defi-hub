package io.defitrack.protocol.idex

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class IdexPoolingMarketProvider(
    private val idexService: IdexService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets() = coroutineScope {
        idexService.getLPs().map {
            async {
                try {
                    if (it.reserveUsd > BigDecimal.valueOf(10000)) {
                        try {
                            val token = getToken(it.liquidityToken)
                            val token0 = getToken(it.tokenA)
                            val token1 = getToken(it.tokenB)

                            create(
                                address = it.liquidityToken,
                                symbol = token.symbol,
                                identifier = it.liquidityToken,
                                name = "IDEX ${token0.symbol}-${token1.symbol}",
                                tokens = listOf(
                                    token0.toFungibleToken(),
                                    token1.toFungibleToken(),
                                ),
                                apr = BigDecimal.ZERO,
                                marketSize = Refreshable.refreshable(it.reserveUsd),
                                positionFetcher = defaultPositionFetcher(token.address),
                                totalSupply = Refreshable.refreshable(token.totalSupply.asEth(token.decimals)) {
                                    getToken(it.liquidityToken).totalDecimalSupply()
                                }
                            )
                        } catch (ex: Exception) {
                            logger.error("something went wrong while importing ${it.liquidityToken}", ex)
                            null
                        }
                    } else {
                        null
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}