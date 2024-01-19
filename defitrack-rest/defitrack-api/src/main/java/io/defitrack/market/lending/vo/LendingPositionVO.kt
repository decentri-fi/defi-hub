package io.defitrack.market.lending.vo

import io.defitrack.domain.FungibleToken
import io.defitrack.domain.NetworkInformation
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
    val token: FungibleToken,
    val exitPositionSupported: Boolean,
    val marketType: String = "lending",
    val market: LendingMarketVO,
)