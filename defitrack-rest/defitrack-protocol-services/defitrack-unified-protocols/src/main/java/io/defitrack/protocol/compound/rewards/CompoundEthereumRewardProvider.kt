package io.defitrack.protocol.compound.rewards

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundEthereumRewardProvider(
) : CompoundRewardProvider() {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}