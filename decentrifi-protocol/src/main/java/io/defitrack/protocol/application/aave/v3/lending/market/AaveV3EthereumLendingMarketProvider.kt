package io.defitrack.protocol.application.aave.v3.lending.market

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AAVE)
class AaveV3EthereumLendingMarketProvider(
    dataProvider: AaveV3DataProvider,
) : AaveV3LendingMarketProvider(
    Network.ETHEREUM, dataProvider
)