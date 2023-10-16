package io.defitrack.protocol.sonne.staking

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sonne.StakedSonneContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SONNE)
class USonneStakingMarketProvider : FarmingMarketProvider() {

    val stakedSonne = "0x41279e29586eb20f9a4f65e031af09fced171166"


    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = StakedSonneContract(getBlockchainGateway(), stakedSonne)
        val stakedtoken = getToken(contract.sonne.await())

        val rewardTokens = contract.tokens().map { this.getToken(it) }

        return listOf(
            create(
                name = "Staked Sonne (uSonne)",
                identifier = stakedSonne,
                stakedToken = stakedtoken.toFungibleToken(),
                rewardTokens = rewardTokens.map(TokenInformationVO::toFungibleToken),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    rewardTokens.map { token ->
                        Reward(
                            token.toFungibleToken(),
                            contract.address,
                            { user ->
                                contract.getClaimableFn(token.address, user)
                            }
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