package io.defitrack.protocol.quickswap.staking

import arrow.core.Either
import arrow.core.some
import arrow.fx.coroutines.parMap
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.QuickswapRewardPoolContract
import io.defitrack.protocol.quickswap.contract.RewardFactoryContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class QuickswapFarmingMarketProvider(
    private val quickswapService: QuickswapService,
) : FarmingMarketProvider() {

    val rewardFactoryContract = lazyAsync {
        RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getRewardFactory(),
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = rewardFactoryContract.await()

        val rewardPools = contract.readMultiCall(
            contract.getStakingTokens().map {
                contract.stakingRewardsInfoByStakingToken(it)
            }
        ).filter { it.success }
            .map { it.data[0].value as String }

        rewardPools.map {
            QuickswapRewardPoolContract(
                getBlockchainGateway(),
                it
            )
        }.parMap(EmptyCoroutineContext, 8) { rewardPool ->
            createMarket(rewardPool)
        }.forEach {
            it.fold(
                { throwable ->
                    logger.error("Failed to create market", throwable)
                },
                { market ->
                    send(market)
                }
            )
        }
    }

    private suspend fun QuickswapFarmingMarketProvider.createMarket(rewardPool: QuickswapRewardPoolContract): Either<Throwable, FarmingMarket> {
        return Either.catch {
            val stakedToken = getToken(rewardPool.stakingTokenAddress())
            val rewardToken = getToken(rewardPool.rewardsTokenAddress())

            val ended = Date(rewardPool.periodFinish().toLong() * 1000).before(Date())

            create(
                identifier = rewardPool.address,
                name = "${stakedToken.name} Reward Pool",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                marketSize = refreshable {
                    getMarketSize(stakedToken.toFungibleToken(), rewardPool.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken.toFungibleToken(),
                        contractAddress = rewardPool.address,
                        getRewardFunction = rewardPool::earned,
                    ),
                    preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
                ),
                balanceFetcher = defaultPositionFetcher(rewardPool.address),
                rewardsFinished = ended
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}