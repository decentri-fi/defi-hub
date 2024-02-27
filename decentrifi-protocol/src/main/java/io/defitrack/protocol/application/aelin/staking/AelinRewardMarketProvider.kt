package io.defitrack.protocol.application.aelin.staking

import io.defitrack.architecture.ConditionalOnCompany
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aelin.StakingRewardsContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AELIN)
class AelinRewardMarketProvider : FarmingMarketProvider() {

    val aelinAddress = "0x61baadcf22d2565b0f471b291c475db5555e0b76"
    val rewardPool by lazy {
        StakingRewardsContract(getBlockchainGateway())
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val aelin = getToken(aelinAddress)
        val rewardToken = aelin

        return listOf(
            create(
                name = "Aelin Staking",
                identifier = "aelin-staking",
                stakedToken = aelin,
                rewardToken = rewardToken,
                marketSize = refreshable {
                    getMarketSize(
                        aelin,
                        rewardPool.address,
                    )
                },
                positionFetcher = defaultPositionFetcher(rewardPool.address),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        rewardPool::earned
                    ),
                    preparedTransaction = selfExecutingTransaction(rewardPool::getRewardFunction)
                )
            ),
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.AELIN
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}