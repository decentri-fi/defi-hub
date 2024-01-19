package io.defitrack.apr

import io.defitrack.common.network.Network
import io.defitrack.token.TokenType
import java.math.BigDecimal

class StakedAsset(
    val address: String,
    val network: Network,
    val amount: BigDecimal,
    val tokenType: TokenType
)