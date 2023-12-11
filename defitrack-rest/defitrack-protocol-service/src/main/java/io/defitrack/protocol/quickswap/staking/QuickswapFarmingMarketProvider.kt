package io.defitrack.protocol.quickswap.staking

import arrow.core.Either
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
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

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = RewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getRewardFactory(),
        )


        contract.getRewardPools().map {
            QuickswapRewardPoolContract(
                getBlockchainGateway(),
                it
            )
        }.parMapNotNull(EmptyCoroutineContext, 8) { rewardPool ->
            Either.catch {
                createMarket(rewardPool)
            }.mapLeft {
                logger.error("Error while fetching quickswap market", it)
                null
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun QuickswapFarmingMarketProvider.createMarket(rewardPool: QuickswapRewardPoolContract): FarmingMarket {
        val stakedToken = getToken(rewardPool.stakingTokenAddress.await())
        val rewardToken = getToken(rewardPool.rewardsTokenAddress.await())

        val ended = Date(rewardPool.periodFinish.await().toLong() * 1000).before(Date())

        return create(
            identifier = rewardPool.address,
            name = "${stakedToken.name} Reward Pool",
            stakedToken = stakedToken,
            rewardToken = rewardToken,
            marketSize = refreshable {
                getMarketSize(stakedToken, rewardPool.address)
            },
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    token = rewardToken,
                    getRewardFunction = rewardPool::earned,
                ),
                preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
            ),
            positionFetcher = defaultPositionFetcher(rewardPool.address),
            deprecated = ended
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}