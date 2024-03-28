package io.defitrack.protocol.application.metavault

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.metavault.StakedMVXContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.METAVAULT)
@Component
class MetavaultStakingProvider : FarmingMarketProvider() {

    val address = "0xe8e2e78d8ca52f238caf69f020fa961f8a7632e9"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = StakedMVXContract(getBlockchainGateway(), address)

        val mvx = getToken("0x2760e46d9bb43dafcbecaad1f64b93207f9f0ed7")
        val rewardToken = getToken(contract.rewardtoken.await())

        return listOf(
            create(
                name = "Staked MVX",
                identifier = address,
                stakedToken = mvx,
                rewardToken = rewardToken,
                positionFetcher = PositionFetcher(
                    contract::balanceOfFn
                ),
                type = "metavault.staking",
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        rewardToken,
                        contract::claimableFn
                    ),
                    preparedTransaction = selfExecutingTransaction(contract::claimFn)
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.METAVAULT
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}