package io.defitrack.protocol.curve.staking

import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.protocol.Protocol

abstract class CurveGaugeFarmingMarketProvider(
    private val gaugeControllerAddress: String
) : FarmingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }


}