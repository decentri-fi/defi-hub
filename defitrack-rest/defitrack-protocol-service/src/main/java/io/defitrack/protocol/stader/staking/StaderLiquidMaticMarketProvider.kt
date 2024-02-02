package io.defitrack.protocol.stader.staking

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STADER)
@ConditionalOnNetwork(Network.POLYGON)
class StaderLiquidMaticMarketProvider: FarmingMarketProvider() {



    override fun getProtocol(): Protocol {
        return Protocol.STADER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}