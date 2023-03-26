package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.crv.CurvePoolGraphProvider
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal

abstract class CurvePoolingMarketProvider(
    private val curvePoolGraphProvider: CurvePoolGraphProvider
) : PoolingMarketProvider() {


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
                            marketSize = marketSizeService.getMarketSize(tokens, lpToken.address, getNetwork()),
                            tokenType = TokenType.CURVE,
                            positionFetcher = if (lpToken.name == "unknown") null else defaultPositionFetcher(lpToken.address),
                            totalSupply = lpToken.totalSupply,
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        logger.error("Unable to import curve pool ${pool.address}", ex.message)
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

    override fun getNetwork(): Network {
        return curvePoolGraphProvider.network
    }
}