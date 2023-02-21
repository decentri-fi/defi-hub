package io.defitrack.farming

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.StargateBinanceService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class StargateBinanceFarmingProvider(
    stargateService: StargateBinanceService,
    accessorGateway: BlockchainGatewayProvider,
    abiResource: ABIResource,
) : StargateFarmingMarketProvider(
    stargateService, accessorGateway, abiResource
) {
    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}