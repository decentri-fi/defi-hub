package io.defitrack.market.domain

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.market.domain.position.ExitPositionCommand
import io.defitrack.protocol.Protocol
import java.math.BigDecimal

data class PoolingMarket(
    override val id: String,
    override val network: Network,
    override val protocol: Protocol,
    override val type: String,
    val address: String,
    val name: String,
    val decimals: Int = 18,
    val symbol: String,
    val totalSupply: Refreshable<BigDecimal>,
    val apr: BigDecimal? = null,
    val marketSize: Refreshable<BigDecimal>? = null,
    val positionFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null,
    val exitPositionPreparer: ExitPositionCommand? = null,
    val breakdown: Refreshable<List<PoolingMarketTokenShare>>,
    val erc20Compatible: Boolean = true,
    val metadata: Map<String, Any>,
    val internalMetadata: Map<String, Any>,
    override val deprecated: Boolean,
) : DefiMarket(id, "pooling", type, protocol, network, deprecated)