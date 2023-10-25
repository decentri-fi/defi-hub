package io.defitrack.price.external

import io.defitrack.common.network.Network
import java.math.BigDecimal

data class ExternalPrice(
    val address: String,
    val network: Network,
    val price: BigDecimal,
    val source: String
)