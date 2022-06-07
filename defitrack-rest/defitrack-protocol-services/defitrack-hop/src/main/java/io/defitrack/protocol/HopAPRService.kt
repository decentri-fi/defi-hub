package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class HopAPRService(
    private val hopService: HopService,
    private val priceResource: PriceResource
) {

    suspend fun getAPR(
        tokenName: String,
        tokenAddress: String,
        tokenDecimals: Int,
        network: Network,
        marketSize: BigDecimal
    ): BigDecimal {
        return try {
            val pairData = hopService.getDailyVolumes(tokenName, network)
            if (pairData.size <= 1) {
                BigDecimal.ZERO
            } else {
                val yearlyTokenFee = pairData.drop(1).map {
                    it.amount.toBigDecimal()
                }.reduce { a, b -> a.plus(b) }.times(BigDecimal.valueOf(0.004)).times(BigDecimal.valueOf(52))
                val yearlyTokenInDollar = priceResource.calculatePrice(
                    PriceRequest(
                        address = tokenAddress,
                        network = network,
                        yearlyTokenFee,
                        type = TokenType.SINGLE
                    )
                )

                yearlyTokenInDollar.toBigDecimal()
                    .divide(marketSize, 18, RoundingMode.HALF_UP)
                    .div(BigDecimal.TEN.pow(tokenDecimals))
            }
        } catch (ex: Exception) {
            BigDecimal.ZERO
        }
    }
}