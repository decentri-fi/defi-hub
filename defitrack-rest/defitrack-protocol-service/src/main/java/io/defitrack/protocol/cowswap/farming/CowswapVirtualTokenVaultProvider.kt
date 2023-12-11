package io.defitrack.protocol.cowswap.farming

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Service

@Service
@ConditionalOnCompany(Company.COWSWAP)
class CowswapVirtualTokenVaultProvider : FarmingMarketProvider() {

    val vtokenAddress = "0xd057b63f5e69cf1b929b356b579cba08d7688048"
    val cowTokenAddress = "0xDEf1CA1fb7FBcDC777520aa7f396b4E015F497aB"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val cowToken = getToken(cowTokenAddress)
        return listOf(
            create(
                name = "Cow Protocol Virtual Token",
                identifier = "vCOW",
                stakedToken = cowToken,
                rewardToken = cowToken,
                marketSize = Refreshable.refreshable {
                    getMarketSize(
                        cowToken, vtokenAddress
                    )
                },
                positionFetcher = defaultPositionFetcher(vtokenAddress),
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