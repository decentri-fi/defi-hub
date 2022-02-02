package io.defitrack.protocol.aave.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarketElement
import io.defitrack.lending.domain.LendingToken
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import io.defitrack.protocol.aave.domain.AaveReserve
import io.defitrack.protocol.staking.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AaveMainnetLendingMarketService(
    private val aaveMainnetService: AaveMainnetService,
    private val priceResource: PriceResource
) : LendingMarketService() {

    override suspend fun fetchLendingMarkets(): List<LendingMarketElement> {
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
                marketSize = calculateMarketSize(it),
                poolType = "aave-v2"
            )
        }
    }

    private fun calculateMarketSize(reserve: AaveReserve): Double {
        return priceResource.calculatePrice(
            PriceRequest(
                reserve.underlyingAsset,
                getNetwork(),
                reserve.totalLiquidity.toBigDecimal().divide(BigDecimal.TEN.pow(reserve.decimals)),
                TokenType.SINGLE
            )
        )
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.ETHEREUM
}