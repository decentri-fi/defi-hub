package io.defitrack.market.farming.vo

import io.defitrack.token.FungibleToken
import io.defitrack.network.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import java.math.BigDecimal

data class FarmingPositionVO(
    val id: String,
    val network: NetworkInformation,
    val protocol: ProtocolInformation,
    val dollarValue: Double,
    val name: String,
    val apr: Double?,
    val stakedToken: FungibleToken,
    val stakedAmountDecimal: BigDecimal,
    val stakedAmount: String,
    val tokenAmountDecimal: BigDecimal,
    val tokenAmount: String,
    val rewardTokens: List<FungibleToken>,
    val exitPositionSupported: Boolean,
    val marketType: String = "farming",
    val expired: Boolean,
    val market: FarmingMarketVO
)