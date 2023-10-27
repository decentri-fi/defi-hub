package io.defitrack.protocol.gains.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.GAINS)
class ArbitrumGainsStakingMarketProvider : GainsStakingMarketProvider(
    "0x7edde7e5900633f698eab0dbc97de640fc5dc015"
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}