package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal
import java.math.BigInteger

data class LendingPositionVO(
    val id: String,
    val network: NetworkInformation,
    val protocol: ProtocolVO,
    val dollarValue: BigDecimal,
    val rate: Double?,
    val name: String,
    val amountDecimal: Double,
    val amount: BigInteger,
    val token: FungibleTokenInformation,
    val exitPositionSupported: Boolean,
    val marketType: String = "lending",
    val market: LendingMarketVO,
)