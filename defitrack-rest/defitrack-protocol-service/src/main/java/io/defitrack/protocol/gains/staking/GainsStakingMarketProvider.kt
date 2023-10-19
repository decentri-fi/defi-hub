package io.defitrack.protocol.gains.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.gains.GainsNetworkStakingContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.GAINS)
class GainsStakingMarketProvider : FarmingMarketProvider() {

    val stakingMarketAddress = "0x7edde7e5900633f698eab0dbc97de640fc5dc015"

    val deferredContract = lazyAsync {
        GainsNetworkStakingContract(
            getBlockchainGateway(),
            stakingMarketAddress
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = deferredContract.await()
        val gns = getToken(contract.gns.await())
        val dai = getToken(contract.dai.await())

        send(
            create(
                name = "Gains Staking",
                identifier = stakingMarketAddress,
                stakedToken = gns.toFungibleToken(),
                rewardTokens = listOf(dai.toFungibleToken()),
                positionFetcher = PositionFetcher(
                    contract.address,
                    { user ->
                        contract.totalGnsStaked(user)
                    }
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        dai.toFungibleToken(),
                        contract.address,
                        { user ->
                            contract.pendingRewardsDai(user)
                        }
                    ),
                    preparedTransaction = {
                        PreparedTransaction(
                            getNetwork().toVO(),
                            contract.harvestDai(),
                            contract.address
                        )
                    }
                ),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.GAINS_NETWORK
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}