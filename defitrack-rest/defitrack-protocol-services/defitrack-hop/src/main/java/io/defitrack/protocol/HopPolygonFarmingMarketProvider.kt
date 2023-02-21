package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.contract.HopStakingReward
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class HopPolygonFarmingMarketProvider(
    private val hopService: HopService,
    private val abiResource: ABIResource,
    private val priceResource: PriceResource
) : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        hopService.getStakingRewards(getNetwork()).map { stakingReward ->
            async(Dispatchers.IO.limitedParallelism(10)) {
                toStakingMarket(stakingReward)

            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun toStakingMarket(stakingReward: String): FarmingMarket? {
        return try {
            val rewardPool = HopStakingReward(
                getBlockchainGateway(),
                abiResource.getABI("quickswap/StakingRewards.json"),
                stakingReward
            )

            val stakedToken = getToken(rewardPool.stakingTokenAddress())
            val rewardToken = getToken(rewardPool.rewardsTokenAddress())

            return create(
                identifier = rewardPool.address,
                name = "${stakedToken.name} Staking Rewards",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                vaultType = "hop-staking-rewards",
                marketSize = getMarketSize(stakedToken, rewardPool),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    address = rewardPool.address,
                    { user ->
                        rewardPool.earnedFunction(user)
                    },
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            rewardPool.getRewardFn(),
                            rewardPool.address,
                            user
                        )
                    }
                ),
                balanceFetcher = PositionFetcher(
                    address = rewardPool.address,
                    function = { user -> rewardPool.balanceOfMethod(user) }
                ),
                farmType = FarmType.LIQUIDITY_MINING
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    private suspend fun getMarketSize(
        stakedTokenInformation: TokenInformationVO,
        pool: HopStakingReward
    ) = BigDecimal.valueOf(
        priceResource.calculatePrice(
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

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}