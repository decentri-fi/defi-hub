package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyBscFarmingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource,
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyService,
    erC20Resource: ERC20Resource,
    priceService: PriceResource
) : BeefyFarmingMarketProvider(
    blockchainGatewayProvider,
    abiResource,
    beefyAPYService,
    beefyService.beefyBscVaults,
    erC20Resource,
    priceService
) {

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}