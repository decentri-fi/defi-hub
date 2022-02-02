package io.defitrack.protocol.aave.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarketElement
import io.defitrack.lending.domain.LendingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AavePolygonService
import org.springframework.stereotype.Service

@Service
class AavePolygonLendingMarketService(private val aavePolygonService: AavePolygonService) : LendingMarketService() {

    override suspend fun fetchLendingMarkets(): List<LendingMarketElement> {
        return aavePolygonService.getReserves().map {
            LendingMarketElement(
                id = "polygon-aave-${it.symbol}",
                address = it.underlyingAsset,
                token = LendingToken(
                    name = it.name,
                    symbol = it.symbol,
                    address = it.underlyingAsset
                ),
                name = it.name + " Aave Pool",
                protocol = getProtocol(),
                network = getNetwork(),
                rate = it.lendingRate,
                marketSize = 0.0,
                poolType = "aave-v2"
            )
        }
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON
}