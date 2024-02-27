package io.defitrack.protocol.application.kwenta

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kwenta.StakingRewardsV2Contract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.KWENTA)
@Component
class KwentaStakingRewardV2MarketProvider : FarmingMarketProvider() {

    val address = "0x61294940ce7cd1bda10e349adc5b538b722ceb88"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = StakingRewardsV2Contract(
            getBlockchainGateway(),
            address
        )

        val kwenta = getToken(contract.kwenta.await())

        return listOf(
            create(
                name = "Kwenta Staking",
                identifier = address,
                stakedToken = kwenta,
                rewardTokens = listOf(kwenta),
                positionFetcher = defaultPositionFetcher(address),
                type = "kwenta.v2.staking-rewards",
                claimableRewardFetcher = ClaimableRewardFetcher(
                    reward = Reward(
                        kwenta,
                        contract::earnedfn
                    ),
                    selfExecutingTransaction(contract::claimFn)
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.KWENTA
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}