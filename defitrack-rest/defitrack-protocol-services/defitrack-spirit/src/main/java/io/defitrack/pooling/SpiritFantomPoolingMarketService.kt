package io.defitrack.pooling

import io.defitrack.SpiritswapAPRService
import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritswapService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SpiritFantomPoolingMarketService(
    private val spiritswapServices: List<SpiritswapService>,
    private val spiritswapAPRService: SpiritswapAPRService,
    private val erC20Resource: ERC20Resource,
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets() = spiritswapServices.filter {
        it.getNetwork() == getNetwork()
    }.flatMap { service ->
        service.getPairs()
            .filter {
                it.reserveUSD > BigDecimal.valueOf(100000)
            }
            .map {

                val token = erC20Resource.getTokenInformation(getNetwork(), it.id)
                val token0 = erC20Resource.getTokenInformation(getNetwork(), it.token0.id)
                val token1 = erC20Resource.getTokenInformation(getNetwork(), it.token1.id)

                PoolingMarketElement(
                    network = service.getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    name = token.name,
                    token = listOf(
                        token0.toFungibleToken(),
                        token1.toFungibleToken(),
                    ),
                    symbol = token.symbol,
                    apr = spiritswapAPRService.getAPR(it.id, service.getNetwork()),
                    id = "spirit-fantom-${it.id}",
                    marketSize = it.reserveUSD,
                    tokenType = TokenType.SPIRIT
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