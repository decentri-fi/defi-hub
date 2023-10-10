package io.defitrack.protocol.chainlink.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.chainlink.ChainlinkStakingContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CHAINLINK)
class ChainlinkStakingMarketProvider : FarmingMarketProvider(

) {

    val link = "0x514910771AF9Ca656af840dff83E8264EcF986CA"

    val chainlinkStakingContract by lazy {
        ChainlinkStakingContract(
            getBlockchainGateway(),
            "0x3feb1e09b4bb0e7f0387cee092a52e85797ab889"
        )
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val chainlinkToken = getToken(link).toFungibleToken()
        return listOf(
            create(
                name = "Chainlink Staking",
                identifier = "chainlink_staking",
                stakedToken = chainlinkToken,
                rewardTokens = listOf(chainlinkToken),
                marketSize = refreshable {
                    getMarketSize(
                        chainlinkToken,
                        chainlinkStakingContract.address,
                    )
                },
                balanceFetcher = PositionFetcher(
                    chainlinkStakingContract.address,
                    { user -> chainlinkStakingContract.getStake(user) }
                ),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CHAINLINK
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}