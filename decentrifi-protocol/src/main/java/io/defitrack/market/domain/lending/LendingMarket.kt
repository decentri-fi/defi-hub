package io.defitrack.market.domain.lending

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.invest.InvestmentPreparer
import io.defitrack.market.domain.DefiMarket
import io.defitrack.market.domain.position.ExitPositionPreparer
import io.defitrack.protocol.Protocol
import java.math.BigDecimal

data class LendingMarket(
    override val id: String,
    override val network: Network,
    override val protocol: Protocol,
    override val type: String,
    val name: String,
    val marketToken: FungibleTokenInformation?,
    val token: FungibleTokenInformation,
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
    id, "lending", type, protocol, network, deprecated
)