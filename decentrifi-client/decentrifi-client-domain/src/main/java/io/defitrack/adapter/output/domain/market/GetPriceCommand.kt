package io.defitrack.adapter.output.domain.market

import io.defitrack.common.network.Network
import io.defitrack.token.TokenType
import java.math.BigDecimal

class GetPriceCommand(
    val address: String,
    val network: Network,
    val amount: BigDecimal,
    val type: TokenType? = null
)