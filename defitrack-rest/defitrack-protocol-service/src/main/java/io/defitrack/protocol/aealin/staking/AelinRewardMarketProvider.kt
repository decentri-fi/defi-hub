package io.defitrack.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aelin.StakingRewardsContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
@ConditionalOnCompany(Company.AELIN)
class AelinRewardMarketProvider(
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    val aelinAddress = "0x61baadcf22d2565b0f471b291c475db5555e0b76"
    val rewardPool by lazy {
        StakingRewardsContract(getBlockchainGateway())
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val aelin = getToken(aelinAddress)
        val rewardToken = aelin

        return listOf(
            create(
                name = "aelin-staking",
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