package io.defitrack.protocol.application.sonne.staking

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sonne.StakedSonneContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SONNE)
class USonneStakingMarketProvider : FarmingMarketProvider() {

    val stakedSonne = "0x41279e29586eb20f9a4f65e031af09fced171166"


    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = StakedSonneContract(stakedSonne)
        val stakedtoken = getToken(contract.sonne.await())

        val rewardTokens = contract.tokens().map { this.getToken(it) }

        return listOf(
            create(
                name = "Staked Sonne (uSonne)",
                identifier = stakedSonne,
                stakedToken = stakedtoken,
                rewardTokens = rewardTokens,
                type = "sonne.usonne",
                claimableRewardFetcher = ClaimableRewardFetcher(
                    rewardTokens.map { token ->
                        Reward(
                            token,
                            contract.getClaimableFor(token.address)
                        )
                    },
                    selfExecutingTransaction(contract::claimAllFn)
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.SONNE
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}