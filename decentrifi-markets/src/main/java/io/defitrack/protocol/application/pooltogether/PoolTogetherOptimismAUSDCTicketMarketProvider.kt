package io.defitrack.protocol.application.pooltogether

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.POOLTOGETHER)
class PoolTogetherOptimismAUSDCTicketMarketProvider : PoolingMarketProvider() {

    val usdcTicketAddress = "0x62bb4fc73094c83b5e952c2180b23fa7054954c4"
    val usdcAddress = "0x7f5c764cbc14f9669b88837ca1490cca17c31607"
    override suspend fun fetchMarkets(): List<PoolingMarket> {

        val token = getToken(usdcTicketAddress)
        val usdc = getToken(usdcAddress)
        val supply = token.totalDecimalSupply()

        return create(
            identifier = "aUSDC-ticket",
            address = usdcTicketAddress,
            name = "PoolTogether aOptUSDC Ticket",
            symbol = "PTaOptUSDC",
            breakdown = refreshable {
                PoolingMarketTokenShare(
                    usdc,
                    token.totalSupply
                ).nel()
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