package io.defitrack.protocol.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundV3BaseLendingMarketProvider(
) : CompoundV3LendingMarketProvider() {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}