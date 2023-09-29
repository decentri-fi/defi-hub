package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class PoolTogetherEthereumAUSDCTicketMarketProvider : PoolingMarketProvider() {

    val usdcTicketAddress = "0xdd4d117723c257cee402285d3acf218e9a8236e1"
    val usdcAddress = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val token = getToken(usdcTicketAddress)

        return listOf(
            create(
                identifier = "aUSDC-ticket",
                address = usdcTicketAddress,
                name = "PoolTogether aUSDC Ticket",
                symbol = "PTaUSDC",
                tokens = listOf(
                    token.toFungibleToken()
                ),
                apr = null,
                marketSize = refreshable {
                    getPriceResource().calculatePrice(
                        PriceRequest(usdcAddress, getNetwork(), token.totalDecimalSupply())
                    ).toBigDecimal()
                },
                positionFetcher = defaultPositionFetcher(token.address),
                totalSupply = refreshable(token.totalDecimalSupply()) {
                    getToken(usdcTicketAddress).totalDecimalSupply()
                }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.POOLTOGETHER
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}