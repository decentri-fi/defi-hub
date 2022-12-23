package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.apr.SushiPoolingAPRCalculator
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import java.math.BigDecimal

abstract class DefaultSushiPoolingMarketService(
    private val sushiServices: List<SushiswapService>,
    erC20Resource: ERC20Resource
) : PoolingMarketProvider(erC20Resource) {

    override suspend fun fetchMarkets() = sushiServices.filter { sushiswapService ->
        sushiswapService.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }
            .map {
                val token = getToken(it.id)
                val token0 = getToken(it.token0.id)
                val token1 = getToken(it.token1.id)

                val element = PoolingMarket(
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
                    tokenType = TokenType.SUSHISWAP,
                    balanceFetcher = defaultBalanceFetcher(token.address)
                )
                element
            }
    }
}