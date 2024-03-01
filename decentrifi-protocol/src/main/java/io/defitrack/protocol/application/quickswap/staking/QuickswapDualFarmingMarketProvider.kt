package io.defitrack.protocol.quickswap.staking

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
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
@ConditionalOnNetwork(Network.POLYGON)
class QuickswapDualFarmingMarketProvider(
    private val quickswapService: QuickswapService,
) : FarmingMarketProvider() {

    context(BlockchainGateway)
    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = DualRewardFactoryContract(
            quickswapService.getDualRewardFactory(),
        )
        val dualPools = contract.getStakingTokens().map {
            contract.stakingRewardsInfoByStakingToken(it)
        }

        dualPools.map {
            quickswapDualRewardPoolContract(it)
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

    private fun quickswapDualRewardPoolContract(address: String) = with(getBlockchainGateway()) {
        QuickswapDualRewardPoolContract(
            address
        )
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
                getMarketSize(stakedToken, pool.address)
            },
            positionFetcher = PositionFetcher(
                stakedToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
            ),
            deprecated = ended,
            type = "quickswap.dual-farming"
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}