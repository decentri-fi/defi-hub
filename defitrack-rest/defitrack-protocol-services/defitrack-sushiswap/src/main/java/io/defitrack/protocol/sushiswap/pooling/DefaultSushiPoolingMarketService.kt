package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.apr.SushiPoolingAPRCalculator
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import java.math.BigDecimal

abstract class DefaultSushiPoolingMarketService(
    private val sushiServices: List<SushiswapService>,
    private val erC20Resource: ERC20Resource
) : PoolingMarketProvider() {

    override suspend fun fetchPoolingMarkets() = sushiServices.filter { sushiswapService ->
        sushiswapService.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }
            .map {
                val token = erC20Resource.getTokenInformation(getNetwork(), it.id)
                val token0 = erC20Resource.getTokenInformation(getNetwork(), it.token0.id)
                val token1 = erC20Resource.getTokenInformation(getNetwork(), it.token1.id)

                val element = PoolingMarketElement(
                    network = service.getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    name = token.name,
                    symbol = token.symbol,
                    tokens = listOf(
                        token0.toFungibleToken(),
                        token1.toFungibleToken(),
                    ),
                    apr = SushiPoolingAPRCalculator(service, it.id).calculateApr(),
                    id = "sushi-${getNetwork().slug}-${it.id}",
                    marketSize = it.reserveUSD,
                    tokenType = TokenType.SUSHISWAP
                )
                element
            }
    }
}