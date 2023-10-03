package io.defitrack.protocol.convex.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexEthereumService
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CONVEX)
class ConvexEthereumRewardPoolMarketProvider(
    private val convexService: ConvexEthereumService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        return convexService.providePools().map {
            CvxRewardPoolContract(
                getBlockchainGateway(),
                it,
            )
        }.map {
            val stakingToken = getToken(it.stakingToken())
            val rewardToken = getToken(it.rewardToken())
            create(
                name = "Convex Reward Pool",
                identifier = it.address,
                stakedToken = stakingToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                balanceFetcher = defaultPositionFetcher(
                    it.address
                ),
                farmType = ContractType.STAKING,
                internalMetadata = mapOf("contract" to it)
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}