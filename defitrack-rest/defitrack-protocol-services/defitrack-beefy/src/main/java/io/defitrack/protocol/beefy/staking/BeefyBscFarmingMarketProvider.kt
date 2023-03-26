package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import org.springframework.stereotype.Service

@Service
class BeefyBscFarmingMarketProvider(
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyService,
) : BeefyFarmingMarketProvider(
    beefyAPYService,
    beefyService.beefyBscVaults,
) {

    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}