package io.defitrack.pool.domain

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import java.math.BigDecimal

class PoolingElement(
    val lpAddress: String,
    val amount: BigDecimal,
    val name: String,
    val symbol: String,
    val network: Network,
    val protocol: Protocol,
    val tokenType: TokenType = TokenType.SINGLE,
    val id: String,
    val apr: BigDecimal? = null,
    val marketSize: BigDecimal? = null,
    val market: PoolingMarketElement
)