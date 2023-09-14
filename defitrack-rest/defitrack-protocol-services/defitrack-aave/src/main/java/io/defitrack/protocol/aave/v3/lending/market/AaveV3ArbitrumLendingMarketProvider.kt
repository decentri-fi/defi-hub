package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.common.network.Network
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
class AaveV3ArbitrumLendingMarketProvider(
    aaveV3DataProvider: AaveV3DataProvider,
) : AaveV3LendingMarketProvider(
    Network.ARBITRUM, aaveV3DataProvider
)