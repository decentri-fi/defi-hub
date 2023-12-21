package io.defitrack.protocol.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundV3ArbitrumLendingMarketProvider : CompoundV3LendingMarketProvider() {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}