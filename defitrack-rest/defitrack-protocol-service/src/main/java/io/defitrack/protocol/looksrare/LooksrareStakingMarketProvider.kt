package io.defitrack.protocol.looksrare

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.LOOKSRARE)
@Component
class LooksrareStakingMarketProvider : FarmingMarketProvider() {


    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = FeeSharingSystemContract(
            getBlockchainGateway(),
            "0xbcd7254a1d759efa08ec7c3291b2e85c5dcc12ce"
        )

        val staked = getToken(contract.looksRareToken.await())
        val reward = getToken(contract.rewardToken.await())

        send(
            create(
                name = "Looksrare Fee Sharing",
                identifier = contract.address,
                stakedToken = staked,
                rewardToken = reward,
                positionFetcher = PositionFetcher(
                    contract::calculateSharesValueInLooks
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        reward,
                        contract::calculatePendingRewards
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::harvest)
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