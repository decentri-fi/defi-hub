package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class AaveV3PolygonLendingMarketProvider(
    abiResource: ABIResource,
    aaveV3DataProvider: AaveV3DataProvider,
    priceResource: PriceResource
) : AaveV3LendingMarketProvider(
    priceResource, Network.POLYGON, abiResource, aaveV3DataProvider
)