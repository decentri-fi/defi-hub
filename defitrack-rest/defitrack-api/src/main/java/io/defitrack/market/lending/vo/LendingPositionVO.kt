package io.defitrack.market.lending.vo

import io.defitrack.token.FungibleToken
import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal
import java.math.BigInteger

data class LendingPositionVO(
    val id: String,
    val network: NetworkInformation,
    val protocol: ProtocolInformation,
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