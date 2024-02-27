package io.defitrack.protocol.application.compound.lending

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundV3PolygonLendingMarketProvidero : CompoundV3LendingMarketProvider() {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}