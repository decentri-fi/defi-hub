package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import org.springframework.stereotype.Service

@Service
class CurveEthereumFarmingMarketProvider : CurveGaugeFarmingMarketProvider(
    "0x2F50D538606Fa9EDD2B11E2446BEb18C9D5846bB"
) {

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}