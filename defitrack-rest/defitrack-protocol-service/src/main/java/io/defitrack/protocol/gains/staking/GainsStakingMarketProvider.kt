package io.defitrack.protocol.gains.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
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
                rewardTokens = listOf(dai),
                positionFetcher = PositionFetcher(
                    contract.address,
                    contract::totalGnsStaked
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        dai.toFungibleToken(),
                        contract.address,
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