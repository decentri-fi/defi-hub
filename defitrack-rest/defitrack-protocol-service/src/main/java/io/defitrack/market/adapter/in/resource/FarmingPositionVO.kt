package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class FarmingPositionVO(
    val id: String,
    val network: NetworkInformation,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val apr: Double?,
    val stakedToken: FungibleTokenInformation,
    val stakedAmountDecimal: BigDecimal,
    val stakedAmount: String,
    val tokenAmountDecimal: BigDecimal,
    val tokenAmount: String,
    val rewardTokens: List<FungibleTokenInformation>,
    val exitPositionSupported: Boolean,
    val marketType: String = "farming",
    val expired: Boolean,
    val market: FarmingMarketVO
)