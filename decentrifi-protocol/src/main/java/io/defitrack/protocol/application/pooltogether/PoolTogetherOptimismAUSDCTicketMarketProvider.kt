package io.defitrack.protocol.application.pooltogether

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.POOLTOGETHER)
class PoolTogetherOptimismAUSDCTicketMarketProvider(
    private val priceResource: PricePort,
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