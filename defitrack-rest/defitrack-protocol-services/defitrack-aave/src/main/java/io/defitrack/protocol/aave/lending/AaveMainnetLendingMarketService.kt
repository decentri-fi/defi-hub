package io.defitrack.protocol.aave.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import io.defitrack.protocol.aave.domain.AaveReserve
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AaveMainnetLendingMarketService(
    private val aaveMainnetService: AaveMainnetService,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
) : LendingMarketService() {

    override suspend fun fetchLendingMarkets(): List<LendingMarket> {
        return aaveMainnetService.getReserves().map {
            val token = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
            LendingMarket(
                id = "ethereum-aave-${it.symbol}",
                address = it.underlyingAsset,
                token = token.toFungibleToken(),
                name = it.name + " Aave Pool",
                protocol = getProtocol(),
                network = getNetwork(),
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