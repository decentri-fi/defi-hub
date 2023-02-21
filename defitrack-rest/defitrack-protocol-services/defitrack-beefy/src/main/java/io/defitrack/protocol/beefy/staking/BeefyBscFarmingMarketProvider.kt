package io.defitrack.protocol.beefy.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import org.springframework.stereotype.Service

@Service
class BeefyBscFarmingMarketProvider(
    abiResource: ABIResource,
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyService,
    priceService: PriceResource
) : BeefyFarmingMarketProvider(
    abiResource,
    beefyAPYService,
    beefyService.beefyBscVaults,
    priceService
) {

    override fun getProtocol(): Protocol {
        return Protocol.BEEFY
    }

    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}