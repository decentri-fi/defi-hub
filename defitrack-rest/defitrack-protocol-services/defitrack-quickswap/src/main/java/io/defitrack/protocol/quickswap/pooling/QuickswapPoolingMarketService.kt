package io.defitrack.protocol.quickswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.domain.QuickswapPair
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@EnableScheduling
class QuickswapPoolingMarketService(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
    private val erC20Resource: ERC20Resource
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        quickswapService.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }.map {
                async(Dispatchers.IO.limitedParallelism(5)) {
                    try {
                        toPoolingMarket(it)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        null
                    }
                }
            }.awaitAll().filterNotNull()
    }

    private suspend fun toPoolingMarket(it: QuickswapPair): PoolingMarket {
        val token = erC20Resource.getTokenInformation(getNetwork(), it.id)
        val token0 = erC20Resource.getTokenInformation(getNetwork(), it.token0.id)
        val token1 = erC20Resource.getTokenInformation(getNetwork(), it.token1.id)

        return PoolingMarket(
            network = getNetwork(),
            protocol = getProtocol(),
            address = it.id,
            id = "quickswap-polygon-${it.id}",
            name = token.name,
            symbol = token.symbol,
            tokens = listOf(
                token0.toFungibleToken(),
                token1.toFungibleToken(),
            ),
            apr = quickswapAPRService.getLPAPR(it.id),
            marketSize = it.reserveUSD,
            tokenType = TokenType.QUICKSWAP
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}