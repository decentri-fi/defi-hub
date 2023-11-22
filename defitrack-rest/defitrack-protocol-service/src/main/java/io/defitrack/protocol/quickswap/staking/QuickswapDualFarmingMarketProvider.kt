package io.defitrack.protocol.quickswap.staking

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract.Companion
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DualRewardFactoryContract
import io.defitrack.protocol.quickswap.contract.QuickswapDualRewardPoolContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class QuickswapDualFarmingMarketProvider(
    private val quickswapService: QuickswapService,
) : FarmingMarketProvider() {

    val dualStakingFactory = lazyAsync {

    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = DualRewardFactoryContract(
            getBlockchainGateway(),
            quickswapService.getDualRewardFactory(),
        )
        val dualPools = contract.getStakingTokens().map {
            contract.stakingRewardsInfoByStakingToken(it)
        }

        dualPools.map {
            QuickswapDualRewardPoolContract(
                getBlockchainGateway(),
                it
            )
        }.parMapNotNull(EmptyCoroutineContext, 12) { pool ->
            catch {
                createMarket(pool)
            }.mapLeft {
                logger.error("Error while fetching quickswap market", it)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun createMarket(pool: QuickswapDualRewardPoolContract): FarmingMarket {
        val stakedToken = getToken(pool.stakingTokenAddress())
        val rewardTokenA = getToken(pool.rewardsTokenAddressA())
        val rewardTokenB = getToken(pool.rewardsTokenAddressB())

        val ended = Date(pool.periodFinish().toLong() * 1000).before(Date())

        return create(
            identifier = pool.address,
            name = "${stakedToken.name} Dual Reward Pool",
            stakedToken = stakedToken,
            rewardTokens = listOf(
                rewardTokenA,
                rewardTokenB
            ),
            marketSize = refreshable {
                getMarketSize(stakedToken.toFungibleToken(), pool.address)
            },
            positionFetcher = PositionFetcher(
                pool.address,
                Companion::balanceOfFunction
            ),
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