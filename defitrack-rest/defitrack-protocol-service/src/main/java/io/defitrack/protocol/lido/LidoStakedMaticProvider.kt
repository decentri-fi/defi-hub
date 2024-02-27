package io.defitrack.protocol.lido

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.LIDO)
class LidoStakedMaticProvider(
    private val lidoService: LidoService,
) : FarmingMarketProvider() {

    val matic = "0x7d1afa7b718fb893db30a3abc0cfc608aacfebb0"

    //TODO: staked matic

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return emptyList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIDO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}