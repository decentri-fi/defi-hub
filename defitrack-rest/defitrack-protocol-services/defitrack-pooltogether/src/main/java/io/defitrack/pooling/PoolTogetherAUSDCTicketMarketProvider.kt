package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class PoolTogetherAUSDCTicketMarketProvider(
    erC20Resource: ERC20Resource,
    private val priceResource: PriceResource
) : PoolingMarketProvider(
    erC20Resource
) {

    val usdcTicketAddress = "0xdd4d117723c257cee402285d3acf218e9a8236e1"
    val usdcAddress = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val token = erc20Resource.getTokenInformation(getNetwork(), usdcTicketAddress)

        return listOf(
            PoolingMarket(
                id = "ethereum-aUSDC-ticket",
                network = getNetwork(),
                protocol = getProtocol(),
                address = usdcTicketAddress,
                name = "PoolTogether aUSDC Ticket",
                symbol = "PTaUSDC",
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
                positionFetcher = defaultBalanceFetcher(
                    token.address
                )
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