package io.defitrack.price.external.adapter.decentrifi

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.BulkConstantResolver
import io.defitrack.port.output.ERC20Client
import io.defitrack.port.output.MarketClient
import io.defitrack.price.external.adapter.stable.StablecoinPriceProvider
import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("oracles.pancakeswap.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiPancakeswapV3PriceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20ClientResource: ERC20Client,
    marketResource: MarketClient,
    stablecoinPriceProvider: StablecoinPriceProvider,
    bulkConstantResolver: BulkConstantResolver
) : DecentrifiUniswapV3BasedPriceService(
    blockchainGatewayProvider,
    erC20ClientResource,
    marketResource,
    stablecoinPriceProvider,
    bulkConstantResolver,
    Protocol.PANCAKESWAP
)