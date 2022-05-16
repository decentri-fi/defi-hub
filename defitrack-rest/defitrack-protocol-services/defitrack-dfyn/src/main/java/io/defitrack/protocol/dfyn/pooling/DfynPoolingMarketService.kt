package io.defitrack.protocol.dfyn.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dfyn.DfynService
import io.defitrack.protocol.dfyn.apr.DfynAPRService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DfynPoolingMarketService(
    private val dfynService: DfynService,
    private val dfynAPRService: DfynAPRService,
    private val erc20Resource: ERC20Resource
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return dfynService.getPairs().mapNotNull {
            if (it.reserveUSD > BigDecimal.valueOf(100000)) {
                val token = erc20Resource.getTokenInformation(getNetwork(), it.id)
                val token0 = erc20Resource.getTokenInformation(getNetwork(), it.token0.id)
                val token1 = erc20Resource.getTokenInformation(getNetwork(), it.token1.id)

                PoolingMarketElement(
                    network = getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    id = "dfyn-polygon-${it.id}",
                    name = token.name,
                    symbol = token.symbol,
                    tokens = listOf(
                        token0.toFungibleToken(),
                        token1.toFungibleToken()
                    ),
                    apr = dfynAPRService.getAPR(it.id),
                    marketSize = it.reserveUSD,
                    tokenType = TokenType.DFYN
                )
            } else {
                null
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DFYN
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}