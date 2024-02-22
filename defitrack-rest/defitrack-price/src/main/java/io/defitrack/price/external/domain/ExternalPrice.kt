package io.defitrack.price.external.domain

import io.defitrack.common.network.Network
import java.math.BigDecimal

data class ExternalPrice(
    val address: String,
    val network: Network,
    val price: BigDecimal,
    val source: String,
    val pair: String,
    val order: Int
)

val NO_EXTERNAL_PRICE = ExternalPrice(
    address = "",
    network = Network.ETHEREUM,
    price = BigDecimal.ZERO,
    source = "none",
    pair = "none",
    order = 0
)
