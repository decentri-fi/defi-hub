package io.defitrack.protocol.dfyn.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dfyn.DfynService
import io.defitrack.protocol.dfyn.apr.DfynAPRService
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DfynPoolingMarketService(
    private val dfynService: DfynService,
    private val dfynAPRService: DfynAPRService,
) : PoolingMarketService() {

    override fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return dfynService.getPairs().mapNotNull {
            if (it.reserveUSD > BigDecimal.valueOf(100000)) {
                PoolingMarketElement(
                    network = getNetwork(),
                    protocol = getProtocol(),
                    address = it.id,
                    id = "dfyn-polygon-${it.id}",
                    name = "DFYN ${it.token0.symbol}-${it.token1.symbol}",
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
                    apr = dfynAPRService.getAPR(it.id),
                    marketSize = it.reserveUSD
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