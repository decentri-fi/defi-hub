package io.defitrack.protocol.quickswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.domain.QuickswapPair
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class QuickswapPoolingMarketProvider(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        quickswapService.getPairs().forEach {
            throttled {
                launch {
                    try {
                        toPoolingMarket(it)?.let {
                            send(it)
                        }
                    } catch (ex: Exception) {
                        logger.error("Unable to import quickswap pair ${it.id}", ex)
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    private suspend fun toPoolingMarket(it: QuickswapPair): PoolingMarket? {
        val token0 = getToken(it.token0.id)
        val token1 = getToken(it.token1.id)
        if (token0.symbol == "UNKWN" || token1.symbol == "UNKWN") return null

        val token = getToken(it.id)

        return create(
            address = it.id,
            identifier = it.id,
            name = token.name,
            symbol = token.symbol,
            tokens = listOf(
                token0.toFungibleToken(),
                token1.toFungibleToken(),
            ),
            apr = quickswapAPRService.getLPAPR(it.id),
            marketSize = refreshable(it.reserveUSD),
            tokenType = TokenType.QUICKSWAP,
            positionFetcher = defaultPositionFetcher(token.address),
            totalSupply = refreshable(token.totalSupply.asEth(token.decimals)) {
                val token = getToken(it.id)
                token.totalSupply.asEth(token.decimals)
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}