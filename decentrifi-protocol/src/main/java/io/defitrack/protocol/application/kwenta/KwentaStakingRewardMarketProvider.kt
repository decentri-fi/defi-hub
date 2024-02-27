package io.defitrack.protocol.application.kwenta

import arrow.core.nel
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kwenta.StakingRewardsContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.KWENTA)
@Component
class KwentaStakingRewardMarketProvider : FarmingMarketProvider() {

    val address = "0x6e56a5d49f775ba08041e28030bc7826b13489e0"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = StakingRewardsContract(
            getBlockchainGateway(),
            address
        )

        val kwenta = getToken(contract.kwenta.await())

        return create(
            name = "Kwenta Staking",
            identifier = address,
            stakedToken = kwenta,
            rewardToken = kwenta,
            positionFetcher = defaultPositionFetcher(address),
            type = "kwenta.staking-rewards",
            claimableRewardFetcher = ClaimableRewardFetcher(
                reward = Reward(
                    kwenta,
                    contract::earnedfn
                ),
                selfExecutingTransaction(contract::claimFn)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol = Protocol.KWENTA

    override fun getNetwork(): Network = Network.OPTIMISM
}