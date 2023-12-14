package io.defitrack.protocol.traderjoe

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.TRADER_JOE)
class StableJoeStakingFarmingMarketProvider : FarmingMarketProvider() {

    val stableJoeStakingAddress = "0x43646a8e839b2f2766392c1bf8f60f6e587b6960"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = StableJoeStakingContract(
            blockchainGateway = getBlockchainGateway(),
            address = stableJoeStakingAddress
        )
        val stakedToken = getToken(contract.joe.await())
        val rewards = contract.rewardTokens().map {
            getToken(it)
        }
        send(
            create(
                name = "Trader Joe Staking Market",
                identifier = stableJoeStakingAddress,
                stakedToken = stakedToken,
                rewardTokens = rewards,
                positionFetcher = PositionFetcher(
                    { user -> contract.getUserInfofn(user, stakedToken.address) }
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    rewards.map { reward ->
                        Reward(
                            reward,
                            { user -> contract.getUserInfofn(user, reward.address) },
                        )
                    },
                    preparedTransaction = selfExecutingTransaction(contract::harvest)
                )
            ),
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.TRADER_JOE
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}