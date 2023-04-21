package io.defitrack.price.hop

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.CamelotService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CamelotPriceService(
    private val camelotService: CamelotService,
    private val erC20Resource: ERC20Resource,
    private val marketSizeService: MarketSizeService
) {


    suspend fun calculateAlgebraPrice(priceRequest: PriceRequest): BigDecimal {
        return camelotService.getPools().find {
            priceRequest.address.lowercase() == it.address.lowercase()
        }?.let {

            val token0 = erC20Resource.getTokenInformation(priceRequest.network, it.token0())
            val token1 = erC20Resource.getTokenInformation(priceRequest.network, it.token1())

            val marketSize = marketSizeService.getMarketSize(
                listOf(token0.toFungibleToken(), token1.toFungibleToken()), it.address, priceRequest.network
            )

            val ratio = priceRequest.amount.dividePrecisely(it.liquidity().asEth())

            ratio.times(marketSize)
        } ?: BigDecimal.ZERO
    }

}