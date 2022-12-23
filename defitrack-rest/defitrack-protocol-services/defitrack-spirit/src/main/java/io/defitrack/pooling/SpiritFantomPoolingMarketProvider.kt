package io.defitrack.pooling

import io.defitrack.apr.SpiritswapAPRService
import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritswapService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SpiritFantomPoolingMarketProvider(
    private val spiritswapServices: List<SpiritswapService>,
    private val spiritswapAPRService: SpiritswapAPRService,
    erC20Resource: ERC20Resource,
) : PoolingMarketProvider(erC20Resource) {

    override suspend fun fetchMarkets() = spiritswapServices.filter {
        it.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }
            .map {

                val token = erc20Resource.getTokenInformation(getNetwork(), it.id)
                val token0 = erc20Resource.getTokenInformation(getNetwork(), it.token0.id)
                val token1 = erc20Resource.getTokenInformation(getNetwork(), it.token1.id)

                PoolingMarket(
                    network = service.getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    name = token.name,
                    tokens = listOf(
                        token0.toFungibleToken(),
                        token1.toFungibleToken(),
                    ),
                    symbol = token.symbol,
                    apr = spiritswapAPRService.getAPR(it.id, service.getNetwork()),
                    id = "spirit-fantom-${it.id}",
                    marketSize = it.reserveUSD,
                    tokenType = TokenType.SPIRIT,
                    balanceFetcher = defaultBalanceFetcher(token.address)
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