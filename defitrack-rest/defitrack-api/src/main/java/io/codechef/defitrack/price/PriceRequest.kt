package io.codechef.defitrack.price

import io.defitrack.common.network.Network
import io.defitrack.protocol.staking.TokenType
import java.math.BigDecimal

class PriceRequest(
    val address: String,
    val network: Network,
    val amount: BigDecimal,
    val type: TokenType?
)