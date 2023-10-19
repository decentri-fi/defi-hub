package io.defitrack.protocol.hop.farming

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import io.defitrack.protocol.hop.contract.HopStakingRewardContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.EmptyCoroutineContext

abstract class HopFarmingMarketProvider(
    private val hopService: HopService,
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        hopService.getStakingRewardsFromJson(getNetwork()).parMapNotNull(EmptyCoroutineContext, 8) { stakingReward ->
            toStakingMarket(stakingReward)
        }.forEach {
            send(it)
        }
    }


    private suspend fun toStakingMarket(stakingReward: String): FarmingMarket? {
        return try {
            val contract = HopStakingRewardContract(
                getBlockchainGateway(),
                stakingReward
            )

            val stakedToken = getToken(contract.stakingTokenAddress())
            val rewardToken = getToken(contract.rewardsTokenAddress())

            return create(
                identifier = contract.address,
                name = "${stakedToken.name} Staking Rewards",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                marketSize = Refreshable.refreshable {
                    getMarketSize(stakedToken, contract)
                },
                positionFetcher = PositionFetcher(
                    address = contract.address,
                    function = { user -> balanceOfFunction(user) }
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken.toFungibleToken(),
                        contractAddress = contract.address,
                        getRewardFunction = { user -> contract.earnedFn(user) },
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            contract.getRewardFn(),
                            contract.address,
                            user
                        )
                    }
                ),
            )
        } catch (ex: Exception) {
            logger.info("Error while fetching staking market $stakingReward", ex)
            null
        }
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: HopStakingRewardContract
    ) = BigDecimal.valueOf(
        getPriceResource().calculatePrice(
            PriceRequest(
                address = stakedTokenInformation.address,
                network = getNetwork(),
                amount = pool.totalSupply().toBigDecimal().divide(
                    BigDecimal.TEN.pow(stakedTokenInformation.decimals), RoundingMode.HALF_UP
                ),
                type = stakedTokenInformation.type
            )
        )
    )

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}