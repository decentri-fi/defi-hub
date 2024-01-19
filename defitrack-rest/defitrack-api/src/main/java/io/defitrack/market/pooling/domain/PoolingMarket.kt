package io.defitrack.market.pooling.domain

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.domain.FungibleToken
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.exit.ExitPositionCommand
import io.defitrack.market.DefiMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.pooling.history.HistoricEventExtractor
import io.defitrack.protocol.Protocol
import java.math.BigDecimal

data class PoolingMarket(
    override val id: String,
    override val network: Network,
    override val protocol: Protocol,
    val address: String,
    val name: String,
    val decimals: Int = 18,
    val symbol: String,
    val totalSupply: Refreshable<BigDecimal>,
    val tokens: List<FungibleToken>,
    val apr: BigDecimal? = null,
    val marketSize: Refreshable<BigDecimal>? = null,
    val positionFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null,
    val exitPositionPreparer: ExitPositionCommand? = null,
    val breakdown: Refreshable<List<PoolingMarketTokenShare>>? = null,
    val erc20Compatible: Boolean = true,
    val price: Refreshable<BigDecimal>,
    val metadata: Map<String, Any>,
    val internalMetadata: Map<String, Any>,
    override val deprecated: Boolean,
    val historicEventExtractor: HistoricEventExtractor? = null,
) : DefiMarket(id, "pooling", protocol, network, deprecated) {
    init {
        addRefetchableValue(breakdown)
        addRefetchableValue(totalSupply)
        addRefetchableValue(marketSize)
        addRefetchableValue(price)
    }
}