package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.adamant.LidoService
import io.defitrack.protocol.adamant.StethContract
import org.springframework.stereotype.Component

@Component
class StethMarketProvider(
    private val lidoService: LidoService
) : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val steth = StethContract(
            getBlockchainGateway(),
            lidoService.steth()
        )

        return listOf(
            create(
                name = "Liquid Staked Ether 2.0",
                identifier = "steth",
                stakedToken = getToken("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2").toFungibleToken(),
                rewardTokens = emptyList(),
                vaultType = "steth",
                farmType = FarmType.STAKING,
                balanceFetcher = PositionFetcher(
                    address = steth.address,
                    function = { user ->
                        steth.sharesOfFunction(user)
                    },
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM

    }
}