package io.defitrack.market.lending.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

data class LendingPositionVO(
    val id: String,
    val network: NetworkVO,
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