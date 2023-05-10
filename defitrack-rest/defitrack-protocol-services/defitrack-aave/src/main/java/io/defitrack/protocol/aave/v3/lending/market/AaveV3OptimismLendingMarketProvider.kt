package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.price.PriceResource
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import org.springframework.stereotype.Component

@Component
class AaveV3OptimismLendingMarketProvider(
    abiResource: ABIResource,
    aaveV3DataProvider: AaveV3DataProvider,
    priceResource: PriceResource
) : AaveV3LendingMarketProvider(
    priceResource, Network.OPTIMISM, abiResource, aaveV3DataProvider
)