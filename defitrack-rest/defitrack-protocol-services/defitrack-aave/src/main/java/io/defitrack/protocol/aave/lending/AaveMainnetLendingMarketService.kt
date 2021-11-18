package io.defitrack.protocol.aave.lending

import io.codechef.defitrack.lending.LendingMarketService
import io.codechef.defitrack.lending.domain.LendingMarketElement
import io.codechef.defitrack.lending.domain.LendingToken
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import org.springframework.stereotype.Service

@Service
class AaveMainnetLendingMarketService(private val aaveMainnetService: AaveMainnetService) : LendingMarketService {

    override fun getLendingMarkets(): List<LendingMarketElement> {
        return aaveMainnetService.getReserves().map {
            LendingMarketElement(
                id = "ethereum-aave-${it.symbol}",
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

    override fun getNetwork(): Network = Network.ETHEREUM
}