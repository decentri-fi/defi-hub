package io.defitrack.protocol.dfyn.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dfyn.DfynService
import io.defitrack.protocol.dfyn.apr.DfynAPRService
import io.defitrack.protocol.dfyn.domain.Pair
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DfynPoolingMarketProvider(
    private val dfynService: DfynService,
    private val dfynAPRService: DfynAPRService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        dfynService.getPairs().map {
            async {
                throttled {
                    createMarket(it)
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DFYN
    }

    private suspend fun DfynPoolingMarketProvider.createMarket(it: Pair): PoolingMarket? {
        return try {
            if (it.reserveUSD > BigDecimal.valueOf(100000)) {
                val token = getToken(it.id)
                val token0 = getToken(it.token0.id)
                val token1 = getToken(it.token1.id)

                create(
                    address = it.id,
                    identifier = it.id,
                    name = token.name,
                    symbol = token.symbol,
                    tokens = listOf(
                        token0.toFungibleToken(),
                        token1.toFungibleToken()
                    ),
                    apr = dfynAPRService.getAPR(it.id),
                    marketSize = Refreshable.refreshable(it.reserveUSD),
                    tokenType = TokenType.DFYN,
                    positionFetcher = defaultPositionFetcher(token.address),
                    totalSupply = Refreshable.refreshable(token.totalDecimalSupply()) {
                        val token = getToken(it.id)
                        token.totalDecimalSupply()
                    }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error fetching market", e)
            null
        }
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}