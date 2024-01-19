package io.defitrack.market.borrowing.domain

import io.defitrack.common.network.Network
import io.defitrack.domain.FungibleToken
import io.defitrack.market.DefiMarket
import io.defitrack.protocol.Protocol
import java.math.BigDecimal

data class BorrowMarket(
    override val id: String,
    override val protocol: Protocol,
    override val network: Network,
    val name: String,
    val token: FungibleToken,
    val rate: BigDecimal? = null,
    override val deprecated: Boolean
) : DefiMarket(
    id, "borrowing", protocol, network, deprecated
)