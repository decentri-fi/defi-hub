package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class PoolTogetherOptimismAUSDCTicketMarketProvider(
    private val priceResource: PriceResource
) : PoolingMarketProvider() {

    val usdcTicketAddress = "0x62bb4fc73094c83b5e952c2180b23fa7054954c4"
    val usdcAddress = "0x7f5c764cbc14f9669b88837ca1490cca17c31607"
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val token = getToken(usdcTicketAddress)

        return listOf(
            PoolingMarket(
                id = "ethereum-aUSDC-ticket",
                network = getNetwork(),
                protocol = getProtocol(),
                address = usdcTicketAddress,
                name = "PoolTogether aOptUSDC Ticket",
                symbol = "PTaOptUSDC",
                tokens = listOf(
                    token.toFungibleToken()
                ),
                apr = null,
                marketSize = priceResource.calculatePrice(
                    PriceRequest(
                        usdcAddress, getNetwork(), token.totalSupply.asEth(token.decimals), TokenType.SINGLE
                    )
                ).toBigDecimal(),
                tokenType = TokenType.POOLTOGETHER,
                positionFetcher = defaultPositionFetcher(token.address)
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.POOLTOGETHER
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}