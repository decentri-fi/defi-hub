package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.aave.v3.ArbitrumV3AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AAVE)
class AaveV3ArbitrumLendingMarketProvider(
    arbitrumV3AaveV3DataProvider: ArbitrumV3AaveV3DataProvider,
) : AaveV3LendingMarketProvider(
    Network.ARBITRUM, arbitrumV3AaveV3DataProvider
)