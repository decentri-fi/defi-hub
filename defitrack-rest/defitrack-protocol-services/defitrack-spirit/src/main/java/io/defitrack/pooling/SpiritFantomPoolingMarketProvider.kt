package io.defitrack.pooling

import io.defitrack.apr.SpiritswapAPRService
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritswapService
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SpiritFantomPoolingMarketProvider(
    private val spiritswapServices: List<SpiritswapService>,
    private val spiritswapAPRService: SpiritswapAPRService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets() = spiritswapServices.filter {
        it.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }
            .map {

                val token = getToken(it.id)
                val token0 = getToken(it.token0.id)
                val token1 = getToken(it.token1.id)

                create(
                    address = it.id,
                    name = token.name,
                    tokens = listOf(
                        token0.toFungibleToken(),
                        token1.toFungibleToken(),
                    ),
                    symbol = token.symbol,
                    apr = spiritswapAPRService.getAPR(it.id, service.getNetwork()),
                    identifier = it.id,
                    marketSize = refreshable(it.reserveUSD),
                    tokenType = TokenType.SPIRIT,
                    positionFetcher = defaultPositionFetcher(token.address),
                    totalSupply = Refreshable.refreshable(token.totalDecimalSupply()) {
                        getToken(it.id).totalDecimalSupply()
                    }
                )
            }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPIRITSWAP
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}