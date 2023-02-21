package io.defitrack.protocol.yearn

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class YearnFarmingMarketProvider(
    private val yearnService: YearnService,
) : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return yearnService.provideYearnV2Vaults().map {
            val stakedtoken = getToken(it.token.id).toFungibleToken()
            create(
                name = it.shareToken.name,
                identifier = it.id,
                stakedToken = stakedtoken,
                rewardTokens = listOf(stakedtoken),
                vaultType = "yearn-v2",
                farmType = ContractType.VAULT
            )
        }
    }

    override fun getProtocol(): Protocol {
        TODO("Not yet implemented")
    }

    override fun getNetwork(): Network {
        TODO("Not yet implemented")
    }
}