package io.defitrack.protocol.application.compound.rewards

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundArbitrumRewardProvider : CompoundRewardProvider(Network.ARBITRUM)