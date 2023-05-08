package io.defitrack.protocol.quickswap.staking

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.apr.QuickswapAPRService
import io.defitrack.protocol.quickswap.contract.DualRewardFactoryContract
import io.defitrack.protocol.quickswap.contract.QuickswapDualRewardPoolContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class QuickswapDualFarmingMarketProvider(
    private val quickswapService: QuickswapService,
    private val quickswapAPRService: QuickswapAPRService,
) : FarmingMarketProvider() {

    val stakingRewardsABI by lazy {
        runBlocking {
            getAbi("quickswap/DualStakingRewards.json")
        }
    }

    val dualStakingFactory by lazy {
        runBlocking {
            DualRewardFactoryContract(
                getBlockchainGateway(),
                quickswapService.getDualRewardFactory(),
            )
        }
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {

        val dualPools = dualStakingFactory.getStakingTokens().map {
            dualStakingFactory.stakingRewardsInfoByStakingToken(it)
        }

        dualPools.map {
            QuickswapDualRewardPoolContract(
                getBlockchainGateway(),
                stakingRewardsABI,
                it
            )
        }.forEach { pool ->
            launch {
                try {
                    val stakedToken = getToken(pool.stakingTokenAddress())
                    val rewardTokenA = getToken(pool.rewardsTokenAddressA())
                    val rewardTokenB = getToken(pool.rewardsTokenAddressB())

                    val ended = Date(pool.periodFinish().toLong() * 1000).before(Date())

                    val market = create(
                        identifier = pool.address,
                        name = "${stakedToken.name} Dual Reward Pool",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = listOf(
                            rewardTokenA.toFungibleToken(),
                            rewardTokenB.toFungibleToken()
                        ),
                        vaultType = "quickswap-dual-reward-pool",
                        marketSize = getMarketSize(stakedToken.toFungibleToken(), pool.address),
                        apr = getApr(pool, stakedToken),
                        balanceFetcher = PositionFetcher(
                            pool.address,
                            { user -> pool.balanceOfMethod(user) }
                        ),
                        farmType = ContractType.DUAL_REWARD_MINING,
                        rewardsFinished = ended
                    )

                    send(market)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private suspend fun getApr(
        pool: QuickswapDualRewardPoolContract,
        stakedTokenInformation: TokenInformationVO
    ): BigDecimal {
        return (quickswapAPRService.getDualPoolAPR(pool.address) + quickswapAPRService.getLPAPR(
            stakedTokenInformation.address
        ))
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}