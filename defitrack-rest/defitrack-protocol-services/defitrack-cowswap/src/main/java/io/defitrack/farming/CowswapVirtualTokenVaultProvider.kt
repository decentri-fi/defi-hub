package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Service

@Service
class CowswapVirtualTokenVaultProvider : FarmingMarketProvider() {

    val vtokenAddress = "0xd057b63f5e69cf1b929b356b579cba08d7688048"
    val cowTokenAddress = "0xDEf1CA1fb7FBcDC777520aa7f396b4E015F497aB"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val cowToken = getToken(cowTokenAddress).toFungibleToken()
        return listOf(
            create(
                name = "Cow Protocol Virtual Token",
                identifier = "vCOW",
                stakedToken = cowToken,
                vaultType = "vcow",
                rewardTokens = emptyList(),
                marketSize = getMarketSize(
                    cowToken, vtokenAddress
                ),
                farmType = ContractType.VOTE_ESCROW,
                balanceFetcher = defaultPositionFetcher(vtokenAddress),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.COWSWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}