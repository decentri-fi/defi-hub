package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.common.network.Network
import io.defitrack.protocol.aave.v3.EthereumV3AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
class AaveV3EthereumLendingMarketProvider(
    dataProvider: EthereumV3AaveV3DataProvider,
) : AaveV3LendingMarketProvider(
    Network.ETHEREUM, dataProvider
)