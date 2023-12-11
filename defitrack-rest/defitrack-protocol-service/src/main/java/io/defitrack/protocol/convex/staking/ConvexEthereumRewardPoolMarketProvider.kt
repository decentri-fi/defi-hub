package io.defitrack.protocol.convex.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexEthereumService
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CONVEX)
class ConvexEthereumRewardPoolMarketProvider(
    private val convexService: ConvexEthereumService
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return convexService.providePools().map {
            CvxRewardPoolContract(
                getBlockchainGateway(),
                it,
            )
        }.map { poolContract ->
            val stakingToken = getToken(poolContract.stakingToken.await())
            val rewardToken = getToken(poolContract.rewardToken.await())
            create(
                name = "Convex Reward Pool",
                identifier = poolContract.address,
                stakedToken = stakingToken,
                rewardTokens = listOf(rewardToken),
                positionFetcher = defaultPositionFetcher(
                    poolContract.address
                ),
                internalMetadata = mapOf("contract" to poolContract),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        rewardToken,
                        poolContract::earnedFunction
                    ),
                    selfExecutingTransaction(poolContract::getRewardFunction)
                )
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}