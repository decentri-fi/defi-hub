package io.defitrack.protocol.quickswap.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.*

@Service
class QuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
) : FarmingMarketProvider() {

    val stakingRewardsABI by lazy {
        runBlocking {
            getAbi("quickswap/StakingRewards.json")
        }
    }

    val rewardFactoryContract by lazy {
        runBlocking {
            RewardFactoryContract(
                getBlockchainGateway(),
                quickswapService.getRewardFactory(),
            )
        }
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val rewardPools = rewardFactoryContract.getStakingTokens().map {
            rewardFactoryContract.stakingRewardsInfoByStakingToken(it)
        }

        rewardPools.map {
            QuickswapRewardPoolContract(
                getBlockchainGateway(),
                stakingRewardsABI,
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
                        vaultType = "quickswap-reward-rewardPool",
                        marketSize = getMarketSize(stakedToken.toFungibleToken(), rewardPool.address),
                        apr = (quickswapAPRService.getRewardPoolAPR(rewardPool.address) + quickswapAPRService.getLPAPR(
                            stakedToken.address
                        )),
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            rewardPool.address,
                            { user ->
                                rewardPool.earned(user)
                            },
                            preparedTransaction = {
                                PreparedTransaction(
                                    getNetwork().toVO(), rewardPool.getRewardFunction(), rewardPool.address
                                )
                            }
                        ),
                        balanceFetcher = defaultPositionFetcher(
                            rewardPool.address
                        ),
                        farmType = ContractType.LIQUIDITY_MINING,
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