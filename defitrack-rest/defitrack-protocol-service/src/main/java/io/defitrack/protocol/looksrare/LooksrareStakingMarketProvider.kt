package io.defitrack.protocol.looksrare

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.LOOKSRARE)
@Component
class LooksrareStakingMarketProvider : FarmingMarketProvider() {

    val feeSharingSystemContract = lazyAsync {
        FeeSharingSystemContract(
            getBlockchainGateway(),
            "0xbcd7254a1d759efa08ec7c3291b2e85c5dcc12ce"
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = feeSharingSystemContract.await()
        val staked = getToken(contract.looksRareToken.await())
        val reward = getToken(contract.rewardToken.await())

        send(
            create(
                name = "Looksrare Fee Sharing",
                identifier = contract.address,
                stakedToken = staked.toFungibleToken(),
                rewardTokens = listOf(reward.toFungibleToken()),
                balanceFetcher = PositionFetcher(
                    contract.address,
                    contract::calculateSharesValueInLooks
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        reward.toFungibleToken(),
                        contract.address,
                        contract::calculatePendingRewards
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            contract.harvest(user),
                            from = user
                        )
                    }
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LOOKSRARE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}