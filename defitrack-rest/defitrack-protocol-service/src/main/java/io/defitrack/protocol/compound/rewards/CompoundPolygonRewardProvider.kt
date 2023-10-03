package io.defitrack.protocol.compound.rewards

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundPolygonRewardProvider(
) : CompoundRewardProvider() {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}