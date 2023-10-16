package io.defitrack.protocol.kwenta

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
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

        val kwenta = getToken(contract.kwenta.await()).toFungibleToken()

        return listOf(
            create(
                name = "Kwenta Staking",
                identifier = address,
                stakedToken = kwenta,
                rewardTokens = listOf(kwenta),
                balanceFetcher = defaultPositionFetcher(address),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    reward = Reward(
                        kwenta,
                        address,
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