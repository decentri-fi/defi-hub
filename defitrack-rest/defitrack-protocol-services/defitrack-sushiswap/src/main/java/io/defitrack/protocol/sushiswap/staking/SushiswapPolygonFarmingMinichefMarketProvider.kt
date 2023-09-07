package io.defitrack.protocol.sushiswap.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiPolygonService
import io.defitrack.protocol.reward.MiniChefV2Contract
import io.defitrack.protocol.sushiswap.apr.MinichefStakingAprCalculator
import io.defitrack.token.TokenType
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class SushiswapPolygonFarmingMinichefMarketProvider(
) : SushiMinichefV2FarmingMarketProvider(
    SushiPolygonService.getMiniChefs()
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}