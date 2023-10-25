package io.defitrack.protocol.aave.v2.farming

import arrow.core.nel
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.staking.StakedAaveContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AAVE)
class StakedAaveMarketProvider : FarmingMarketProvider() {

    val stakedAaveAddress = "0x4da27a545c0c5b758a6ba100e3a049001de870f5"
    val aaveAddress = "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val stakedAave = StakedAaveContract(getBlockchainGateway(), stakedAaveAddress)
        val aave = getToken(aaveAddress)

        return create(
            identifier = stakedAaveAddress,
            name = "Staked Aave",
            stakedToken = aave,
            rewardTokens = aave.nel(),
            positionFetcher = defaultPositionFetcher(stakedAaveAddress),
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    aave,
                    stakedAaveAddress,
                    stakedAave::getTotalRewardFunction
                ),
                preparedTransaction = selfExecutingTransaction(stakedAave::getClaimRewardsFunction)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE_V2
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}