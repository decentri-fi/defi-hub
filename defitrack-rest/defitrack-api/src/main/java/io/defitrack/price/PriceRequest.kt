package io.defitrack.price

import io.defitrack.common.network.Network
import java.math.BigDecimal

class PriceRequest(
    val address: String,
    val network: Network,
    val amount: BigDecimal,
    val type: String? = null
)