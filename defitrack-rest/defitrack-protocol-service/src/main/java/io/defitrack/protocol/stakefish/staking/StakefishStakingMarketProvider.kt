package io.defitrack.protocol.stakefish.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stakefish.StakefishFeeRecipientContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STAKEFISH)
class StakefishStakingMarketProvider : FarmingMarketProvider() {

    private val stakefishFeeRecipient = "0xffee087852cb4898e6c3532e776e68bc68b1143b"

    val stakefishStakingContract by lazy {
        StakefishFeeRecipientContract(
            getBlockchainGateway(),
            stakefishFeeRecipient
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> {
        return channelFlow {

            val rewardToken = getToken("0x0").toFungibleToken()

            send(
                create(
                    name = "Stakefish ETH",
                    identifier = "stakefish-eth",
                    stakedToken = rewardToken,
                    rewardTokens = listOf(rewardToken),
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        rewards = listOf(
                            Reward(
                                token = rewardToken,
                                getRewardFunction = stakefishStakingContract::getPendingRewardFunction,
                            )
                        ),
                        preparedTransaction = selfExecutingTransaction(stakefishStakingContract::claimFunction)
                    )
                )
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.STAKEFISH
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}