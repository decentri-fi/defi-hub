package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.aave.v3.OptimismV3AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AAVE)
class AaveV3OptimismLendingMarketProvider(
    dataProvider: OptimismV3AaveV3DataProvider,
) : AaveV3LendingMarketProvider(
    Network.OPTIMISM, dataProvider
)