package io.defitrack.protocol.dfyn.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
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
    erc20Resource: ERC20Resource
) : PoolingMarketProvider(erc20Resource) {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return dfynService.getPairs().mapNotNull {
            if (it.reserveUSD > BigDecimal.valueOf(100000)) {
                val token = getToken(it.id)
                val token0 = getToken(it.token0.id)
                val token1 = getToken(it.token1.id)

                PoolingMarket(
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
                    tokenType = TokenType.DFYN,
                    balanceFetcher = defaultBalanceFetcher(token.address)
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