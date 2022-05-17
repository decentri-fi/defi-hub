package io.defitrack.token

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class MarketSizeService(
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource
) {

    fun getMarketSize(
        token: FungibleToken,
        location: String,
        network: Network
    ): BigDecimal {
        val balance = erC20Resource.getBalance(network, token.address, location).asEth(token.decimals)
        return priceResource.calculatePrice(
            PriceRequest(
                address = token.address,
                network = network,
                amount = balance,
                type = token.type
            )
        ).toBigDecimal()
    }
}