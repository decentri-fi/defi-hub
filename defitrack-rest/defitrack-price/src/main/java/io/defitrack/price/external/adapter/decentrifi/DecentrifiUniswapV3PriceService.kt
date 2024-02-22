package io.defitrack.price.external.adapter.decentrifi

import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.BulkConstantResolver
import io.defitrack.marketinfo.port.out.Markets
import io.defitrack.price.external.adapter.stable.StablecoinPriceProvider
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.protocol.Protocol
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("oracles.uniswap_v3.enabled", havingValue = "true", matchIfMissing = true)
class DecentrifiUniswapV3PriceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
    marketResource: Markets,
    stablecoinPriceProvider: StablecoinPriceProvider,
    bulkConstantResolver: BulkConstantResolver
) : DecentrifiUniswapV3BasedPriceService(
    blockchainGatewayProvider,
    erC20Resource,
    marketResource,
    stablecoinPriceProvider,
    bulkConstantResolver,
    Protocol.UNISWAP_V3
)