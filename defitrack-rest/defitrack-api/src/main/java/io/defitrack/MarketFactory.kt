package io.defitrack

import io.defitrack.abi.ABIResource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import org.springframework.stereotype.Component

@Component
class MarketFactory(
    val abiResource: ABIResource,
    val erC20Resource: ERC20Resource,
    val priceResource: PriceResource,
    val blockchainGatewayProvider: BlockchainGatewayProvider,
    val marketSizeService: MarketSizeService
)