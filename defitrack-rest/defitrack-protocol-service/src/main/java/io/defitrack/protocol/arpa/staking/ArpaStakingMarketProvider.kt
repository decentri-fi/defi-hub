package io.defitrack.protocol.arpa.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.arpa.ArpaStakingContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ARPA)
class ArpaStakingMarketProvider : FarmingMarketProvider() {

    val arpaStakingAddress = "0xEe710f79aA85099e200be4d40Cdf1Bfb2B467a01"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = ArpaStakingContract(
            getBlockchainGateway(),
            arpaStakingAddress
        )

        val arpa = getToken(contract.arpaToken.await())

        send(
            create(
                name = "ARPA Staking",
                identifier = arpaStakingAddress,
                stakedToken = arpa,
                rewardToken = arpa,
                positionFetcher = PositionFetcher(
                    contract::getStakeFn
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        arpa,
                        contract::getBaseReward
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::claimReward)
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.ARPA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}