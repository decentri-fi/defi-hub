package io.defitrack.price.hop

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.price.PriceProvider
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.HopService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class HopPriceService(
    private val hopService: HopService,
    private val erC20Resource: ERC20Resource,
    private val priceProvider: PriceProvider
) {

    fun calculateHopPrice(priceRequest: PriceRequest): BigDecimal {
        return hopService.getLps(priceRequest.network).find {
            it.lpToken.lowercase() == priceRequest.address
        }?.let {

            val canonicalToken = erC20Resource.getTokenInformation(priceRequest.network, it.canonicalToken)
            val hToken = erC20Resource.getTokenInformation(priceRequest.network, it.canonicalToken)
            val lpToken = erC20Resource.getTokenInformation(priceRequest.network, it.lpToken)
            val hTokenBalance = erC20Resource.getBalance(priceRequest.network, it.hToken, it.swapAddress)
            val canonicalTokenBalance =
                erC20Resource.getBalance(priceRequest.network, it.canonicalToken, it.swapAddress)

            val tokenPrice = priceProvider.getPrice(canonicalToken.symbol)

            val totalLpWorth = (hTokenBalance.asEth(hToken.decimals).times(tokenPrice)).plus(
                canonicalTokenBalance.asEth(canonicalToken.decimals).times(tokenPrice)
            )

            val userShares = priceRequest.amount.dividePrecisely(lpToken.totalSupply.asEth(lpToken.decimals))
            totalLpWorth.times(userShares)
        } ?: BigDecimal.ZERO
    }
}