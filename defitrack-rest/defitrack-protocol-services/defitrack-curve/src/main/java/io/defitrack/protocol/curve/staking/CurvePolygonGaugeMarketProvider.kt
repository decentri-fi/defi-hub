package io.defitrack.protocol.curve.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveGaugeContract
import io.defitrack.protocol.crv.contract.CurvePolygonGaugeControllerContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class CurvePolygonGaugeMarketProvider : CurveGaugeFarmingMarketProvider(
    "0xabc000d88f23bb45525e447528dbf656a9d55bf5"
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}