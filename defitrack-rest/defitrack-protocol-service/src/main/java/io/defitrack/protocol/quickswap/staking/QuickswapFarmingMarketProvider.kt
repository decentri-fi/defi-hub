package io.defitrack.protocol.quickswap.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.util.*

@Service
@ConditionalOnCompany(Company.QUICKSWAP)
class QuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
) : FarmingMarketProvider() {

    val rewardFactoryContract = lazyAsync {
        RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getRewardFactory(),
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = rewardFactoryContract.await()
        val rewardPools = contract.getStakingTokens().map {
            contract.stakingRewardsInfoByStakingToken(it)
        }

        rewardPools.map {
            QuickswapRewardPoolContract(
                getBlockchainGateway(),
                it
            )
        }.forEach { rewardPool ->
            launch {
                try {
                    val stakedToken = getToken(rewardPool.stakingTokenAddress())
                    val rewardToken = getToken(rewardPool.rewardsTokenAddress())

                    val ended = Date(rewardPool.periodFinish().toLong() * 1000).before(Date())

                    val market = create(
                        identifier = rewardPool.address,
                        name = "${stakedToken.name} Reward Pool",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = listOf(rewardToken.toFungibleToken()),
                        marketSize = refreshable {
                            getMarketSize(stakedToken.toFungibleToken(), rewardPool.address)
                        },
                        apr = (quickswapAPRService.getRewardPoolAPR(rewardPool.address) + quickswapAPRService.getLPAPR(
                            stakedToken.address
                        )),
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            Reward(
                                token = rewardToken.toFungibleToken(),
                                contractAddress = rewardPool.address,
                                getRewardFunction = { user ->
                                    rewardPool.earned(user)
                                },
                            ),
                            preparedTransaction = {
                                PreparedTransaction(
                                    getNetwork().toVO(), rewardPool.getRewardFunction(), rewardPool.address
                                )
                            }
                        ),
                        balanceFetcher = defaultPositionFetcher(
                            rewardPool.address
                        ),
                        rewardsFinished = ended
                    )
                    send(market)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}