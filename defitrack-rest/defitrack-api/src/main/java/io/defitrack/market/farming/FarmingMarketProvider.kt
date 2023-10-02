package io.defitrack.market.farming

import ClaimableMarketProvider
import io.defitrack.claimable.ClaimableMarket
import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.utils.Refreshable
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.MarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

abstract class FarmingMarketProvider : MarketProvider<FarmingMarket>(), ClaimableMarketProvider {

    fun create(
        name: String,
        identifier: String,
        stakedToken: FungibleToken,
        rewardTokens: List<FungibleToken>,
        marketSize: Refreshable<BigDecimal>? = null,
        apr: BigDecimal? = null,
        balanceFetcher: PositionFetcher? = null,
        claimableRewardFetcher: ClaimableRewardFetcher? = null,
        investmentPreparer: InvestmentPreparer? = null,
        exitPositionPreparer: ExitPositionPreparer? = null,
        farmType: ContractType,
        metadata: Map<String, Any> = emptyMap(),
        internalMetadata: Map<String, Any> = emptyMap(),
        rewardsFinished: Boolean = false
    ): FarmingMarket {
        return FarmingMarket(
            id = "frm_${getNetwork().slug}-${getProtocol().slug}-${identifier}",
            network = getNetwork(),
            protocol = getProtocol(),
            name = name,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            marketSize = marketSize,
            apr = apr,
            farmType = farmType,
            balanceFetcher = balanceFetcher,
            investmentPreparer = investmentPreparer,
            claimableRewardFetcher = claimableRewardFetcher,
            metadata = metadata,
            internalMetadata = internalMetadata,
            expired = rewardsFinished,
            exitPositionPreparer = exitPositionPreparer
        )
    }

    override suspend fun getClaimables(): List<ClaimableMarket> {
        return getMarkets().filter {
            it.claimableRewardFetcher != null
        }.map {
            ClaimableMarket(
                id = "rwrd_" + it.id,
                name = it.name + " reward",
                network = it.network,
                protocol = it.protocol,
                claimableRewardFetcher = it.claimableRewardFetcher!!,
            )
        }
    }
}