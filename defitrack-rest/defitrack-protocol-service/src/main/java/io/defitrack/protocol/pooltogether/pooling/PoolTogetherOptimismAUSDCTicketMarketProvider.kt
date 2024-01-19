package io.defitrack.protocol.pooltogether.pooling

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.domain.GetPriceCommand
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.port.input.PriceResource
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.POOLTOGETHER)
class PoolTogetherOptimismAUSDCTicketMarketProvider(
    private val priceResource: PriceResource
) : PoolingMarketProvider() {

    val usdcTicketAddress = "0x62bb4fc73094c83b5e952c2180b23fa7054954c4"
    val usdcAddress = "0x7f5c764cbc14f9669b88837ca1490cca17c31607"
    override suspend fun fetchMarkets(): List<PoolingMarket> {

        val token = getToken(usdcTicketAddress)
        val supply = token.totalDecimalSupply()

        return create(
            identifier = "aUSDC-ticket",
            address = usdcTicketAddress,
            name = "PoolTogether aOptUSDC Ticket",
            symbol = "PTaOptUSDC",
            tokens = listOf(
                token
            ),
            marketSize = refreshable {
                priceResource.calculatePrice(
                    GetPriceCommand(
                        usdcAddress, getNetwork(), supply
                    )
                ).toBigDecimal()
            },
            positionFetcher = defaultPositionFetcher(token.address),
            totalSupply = refreshable(supply) {
                getToken(usdcTicketAddress).totalDecimalSupply()
            }
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.POOLTOGETHER
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}