package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.apr.SushiPoolingAPRCalculator
import java.math.BigDecimal

abstract class DefaultSushiPoolingMarketService(
    private val sushiServices: List<SushiswapService>,
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets() = sushiServices.filter { sushiswapService ->
        sushiswapService.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }
            .map {
                val element = PoolingMarketElement(
                    network = service.getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    name = "SUSHI ${it.token0.symbol}-${it.token1.symbol}",
                    token = listOf(
                        PoolingToken(
                            it.token0.name,
                            it.token0.symbol,
                            it.token0.id
                        ),
                        PoolingToken(
                            it.token1.name,
                            it.token1.symbol,
                            it.token1.id
                        ),
                    ),
                    apr = SushiPoolingAPRCalculator(service, it.id).calculateApr(),
                    id = "sushi-${getNetwork().slug}-${it.id}",
                    marketSize = it.reserveUSD
                )
                element
            }
    }
}