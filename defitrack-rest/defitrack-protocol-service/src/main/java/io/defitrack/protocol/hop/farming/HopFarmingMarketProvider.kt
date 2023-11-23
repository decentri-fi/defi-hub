package io.defitrack.protocol.hop.farming

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.map
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import io.defitrack.protocol.hop.contract.HopStakingRewardContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal
import kotlin.coroutines.EmptyCoroutineContext

abstract class HopFarmingMarketProvider(
    private val hopService: HopService,
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        hopService.getStakingRewardsFromJson(getNetwork())
            .parMapNotNull(concurrency = 8) { stakingReward ->
                toStakingMarket(stakingReward)
            }.forEach {
                send(it)
            }
    }


    private suspend fun toStakingMarket(stakingReward: String): FarmingMarket {
        val contract = HopStakingRewardContract(
            getBlockchainGateway(),
            stakingReward
        )

        val stakedToken = getToken(contract.stakingTokenAddress())
        val rewardToken = getToken(contract.rewardsTokenAddress())

        return create(
            identifier = contract.address,
            name = "${stakedToken.name} Staking Rewards",
            stakedToken = stakedToken,
            rewardToken = rewardToken,
            marketSize = refreshable {
                getMarketSize(stakedToken, contract)
            },
            positionFetcher = PositionFetcher(contract::balanceOfFunction),
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    rewardToken.toFungibleToken(),
                    contract::earnedFn,
                ),
                preparedTransaction = selfExecutingTransaction(contract::getRewardFn)
            ),
        )
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: HopStakingRewardContract
    ) = BigDecimal.valueOf(
        getPriceResource().calculatePrice(
            PriceRequest(
                address = stakedTokenInformation.address,
                network = getNetwork(),
                amount = pool.totalSupply().map {
                    it.asEth(stakedTokenInformation.decimals)
                }.get(),
                type = stakedTokenInformation.type
            )
        )
    )

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}