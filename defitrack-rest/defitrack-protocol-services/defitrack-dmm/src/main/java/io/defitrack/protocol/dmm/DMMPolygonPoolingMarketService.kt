package io.defitrack.protocol.dmm

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dmm.apr.DMMAPRService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DMMPolygonPoolingMarketService(
    private val dmmPolygonService: DMMPolygonService,
    private val dmmaprService: DMMAPRService,
    private val erc20Resource: ERC20Resource
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return dmmPolygonService.getPoolingMarkets().map {

            val token0 = erc20Resource.getTokenInformation(getNetwork(), it.token0.id)
            val token1 = erc20Resource.getTokenInformation(getNetwork(), it.token1.id)

            PoolingMarketElement(
                id = "dmm-polygon-${it.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.id,
                name = "DMM ${it.token0.symbol}-${it.token1.symbol}",
                token = listOf(
                    token0.toFungibleToken(),
                    token1.toFungibleToken()
                ),
                apr = dmmaprService.getAPR(it.pair.id, getNetwork()),
                marketSize = it.reserveUSD
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DMM
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}