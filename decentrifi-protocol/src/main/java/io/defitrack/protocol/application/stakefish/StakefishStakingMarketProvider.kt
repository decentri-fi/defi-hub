package io.defitrack.protocol.application.stakefish

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stakefish.StakefishFeeRecipientContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STAKEFISH)
class StakefishStakingMarketProvider : FarmingMarketProvider() {

    private val stakefishFeeRecipient = "0xffee087852cb4898e6c3532e776e68bc68b1143b"


    context(BlockchainGateway)
    override suspend fun produceMarkets(): Flow<FarmingMarket> {
        return channelFlow {

            val stakefishStakingContract = StakefishFeeRecipientContract(
                stakefishFeeRecipient
            )

            val eth = getToken("0x0")

            send(
                create(
                    name = "Stakefish ETH",
                    identifier = "stakefish-eth",
                    stakedToken = eth,
                    rewardToken = eth,
                    type = "stakefish.staked-eth",
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        reward = Reward(
                            token = eth,
                            getRewardFunction = stakefishStakingContract::getPendingRewardFunction,
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