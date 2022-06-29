package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class AaveV3FantomLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    aaveV3DataProvider: AaveV3DataProvider,
    priceResource: PriceResource
) : AaveV3LendingMarketProvider(
    erC20Resource, priceResource, Network.FANTOM, blockchainGatewayProvider, abiResource, aaveV3DataProvider
)