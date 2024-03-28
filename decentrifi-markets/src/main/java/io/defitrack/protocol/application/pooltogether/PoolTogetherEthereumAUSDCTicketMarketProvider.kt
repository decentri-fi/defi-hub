package io.defitrack.protocol.application.pooltogether

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.POOLTOGETHER)
class PoolTogetherEthereumAUSDCTicketMarketProvider : PoolingMarketProvider() {

    val usdcTicketAddress = "0xdd4d117723c257cee402285d3acf218e9a8236e1"
    val usdcAddress = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val token = getToken(usdcTicketAddress)
        val usdc = getToken(usdcAddress)

        return create(
            identifier = "aUSDC-ticket",
            address = usdcTicketAddress,
            name = "PoolTogether aUSDC Ticket",
            symbol = "PTaUSDC",
            breakdown = refreshable {
                PoolingMarketTokenShare(
                    usdc,
                    token.totalSupply
                ).nel()
            },
            positionFetcher = defaultPositionFetcher(token.address),
            totalSupply = refreshable(token.totalDecimalSupply()) {
                getToken(usdcTicketAddress).totalDecimalSupply()
            }
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.POOLTOGETHER
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}