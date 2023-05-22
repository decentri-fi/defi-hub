package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
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

        val supply = token.totalDecimalSupply()

        return listOf(
            create(
                identifier = "aUSDC-ticket",
                address = usdcTicketAddress,
                name = "PoolTogether aOptUSDC Ticket",
                symbol = "PTaOptUSDC",
                tokens = listOf(
                    token.toFungibleToken()
                ),
                apr = null,
                marketSize = refreshable {
                    priceResource.calculatePrice(
                        PriceRequest(
                            usdcAddress, getNetwork(), supply
                        )
                    ).toBigDecimal()
                },
                tokenType = TokenType.POOLTOGETHER,
                positionFetcher = defaultPositionFetcher(token.address),
                totalSupply = refreshable(supply) {
                    val token = getToken(usdcTicketAddress)
                    token.totalDecimalSupply()
                }
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