package io.defitrack.protocol.blur.pooling

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BLUR)
class BlurStakingMarketMarketProvider : FarmingMarketProvider() {

    val blurAddress = "0x5283d291dbcf85356a21ba090e6db59121208b44"
    val blurStakingAddress = "0xec2432a227440139ddf1044c3fea7ae03203933e"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val blur = getToken(blurAddress)
        return create(
            name = "BlurEth",
            identifier = blurStakingAddress,
            positionFetcher = defaultPositionFetcher(blurStakingAddress),
            stakedToken = blur
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BLUR
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}