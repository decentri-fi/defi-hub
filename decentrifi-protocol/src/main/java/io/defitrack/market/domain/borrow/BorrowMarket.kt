package io.defitrack.market.domain.borrow

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.DefiMarket
import io.defitrack.protocol.Protocol
import java.math.BigDecimal

data class BorrowMarket(
    override val id: String,
    override val protocol: Protocol,
    override val network: Network,
    val name: String,
    val token: FungibleTokenInformation,
    val rate: BigDecimal? = null,
    override val deprecated: Boolean
) : DefiMarket(
    id, "borrowing", protocol, network, deprecated
)