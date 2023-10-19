package io.defitrack.protocol.metavault

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.METAVAULT)
@Component
class MetavaultStakingProvider : FarmingMarketProvider() {

    val address = "0xe8e2e78d8ca52f238caf69f020fa961f8a7632e9"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = StakedMVXContract(getBlockchainGateway(), address)

        val mvx = getToken("0x2760e46d9bb43dafcbecaad1f64b93207f9f0ed7").toFungibleToken()
        val rewardToken = getToken(contract.rewardtoken.await()).toFungibleToken()

        return listOf(
            create(
                name = "Staked MVX",
                identifier = address,
                stakedToken = mvx,
                rewardTokens = listOf(rewardToken),
                positionFetcher = PositionFetcher(
                    contract.address,
                    contract::balanceOfFn
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        rewardToken,
                        contract.address,
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