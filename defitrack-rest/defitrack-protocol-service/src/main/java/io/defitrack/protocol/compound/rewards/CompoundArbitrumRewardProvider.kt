package io.defitrack.protocol.compound.rewards

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundArbitrumRewardProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider
) : CompoundRewardProvider(
    blockchainGatewayProvider, Network.ARBITRUM
)