package io.defitrack.protocol.curve.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class CurveEthereumFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    curveEthereumFarmingMarketProvider: CurveEthereumFarmingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultFarmingPositionProvider(
    erC20Resource, curveEthereumFarmingMarketProvider, blockchainGatewayProvider
)