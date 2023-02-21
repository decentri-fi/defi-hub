package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ChainlinkStakingContract
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
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
                vaultType = "chainlink",
                marketSize = marketSizeService.getMarketSize(
                    chainlinkToken,
                    chainlinkStakingContract.address,
                    getNetwork()
                ),
                balanceFetcher = PositionFetcher(
                    chainlinkStakingContract.address,
                    { user -> chainlinkStakingContract.getStake(user) }
                ),
                farmType = ContractType.STAKING,
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