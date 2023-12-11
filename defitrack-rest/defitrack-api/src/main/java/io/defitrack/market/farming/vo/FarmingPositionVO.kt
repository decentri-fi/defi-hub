package io.defitrack.market.farming.vo

import io.defitrack.erc20.FungibleToken
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class FarmingPositionVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
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