package io.defitrack.protocol.gains.staking

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.gains.GainsNetworkStakingContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

@ConditionalOnCompany(Company.GAINS)
abstract class GainsStakingMarketProvider(
    private val stakingMarketAddress: String
) : FarmingMarketProvider() {

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = GainsNetworkStakingContract(
            getBlockchainGateway(),
            stakingMarketAddress
        )

        val gns = getToken(contract.gns.await())
        val dai = getToken(contract.dai.await())

        send(
            create(
                name = "Gains Staking",
                identifier = stakingMarketAddress,
                stakedToken = gns,
                rewardToken = dai,
                positionFetcher = PositionFetcher(
                    contract::totalGnsStaked
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        dai,
                        contract::pendingRewardsDai
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::harvestDai)
                ),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.GAINS_NETWORK
    }
}