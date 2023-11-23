package io.defitrack.market.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.DefiMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import java.math.BigDecimal


data class LendingMarket(
    override val id: String,
    override val network: Network,
    override val protocol: Protocol,
    val name: String,
    val marketToken: FungibleToken?,
    val token: FungibleToken,
    val marketSize: Refreshable<BigDecimal>? = null,
    val rate: BigDecimal? = null,
    val poolType: String,
    val positionFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null,
    val exitPositionPreparer: ExitPositionPreparer? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val erc20Compatible: Boolean = false,
    val totalSupply: Refreshable<BigDecimal>,
    override val deprecated: Boolean,
    val price: Refreshable<BigDecimal>,
    val internalMetaData: Map<String, Any> = emptyMap(),
) : DefiMarket(
    id, "lending", protocol, network, deprecated
) {

    init {
        addRefetchableValue(totalSupply)
        addRefetchableValue(marketSize)
        addRefetchableValue(price)
    }
}

