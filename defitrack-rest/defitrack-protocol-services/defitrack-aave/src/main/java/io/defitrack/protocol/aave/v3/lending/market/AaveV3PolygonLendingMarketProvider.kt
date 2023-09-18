package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.common.network.Network
import io.defitrack.protocol.aave.v3.ArbitrumV3AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
class AaveV3PolygonLendingMarketProvider(
    arbitrumV3AaveV3DataProvider: ArbitrumV3AaveV3DataProvider,
) : AaveV3LendingMarketProvider(
    Network.POLYGON, arbitrumV3AaveV3DataProvider
)