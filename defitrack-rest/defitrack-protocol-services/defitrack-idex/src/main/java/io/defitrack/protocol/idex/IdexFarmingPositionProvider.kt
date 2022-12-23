package io.defitrack.protocol.idex

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class IdexFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    idexFarmingMarketService: IdexFarmingMarketProvider,
    gateway: BlockchainGatewayProvider
) : DefaultFarmingPositionProvider(
    erC20Resource, idexFarmingMarketService, gateway
)