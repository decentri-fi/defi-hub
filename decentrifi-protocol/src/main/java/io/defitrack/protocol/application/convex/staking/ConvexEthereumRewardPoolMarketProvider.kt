package io.defitrack.protocol.application.convex.staking

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
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
            getCrvRewardContract(it)
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

    private fun getCrvRewardContract(it: String): CvxRewardPoolContract = with(getBlockchainGateway()){
        return CvxRewardPoolContract(
            it,
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}