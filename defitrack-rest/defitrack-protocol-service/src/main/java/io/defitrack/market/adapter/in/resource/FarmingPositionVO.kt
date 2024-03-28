package io.defitrack.market.adapter.`in`.resource

import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.ProtocolVO
import java.math.BigDecimal

data class FarmingPositionVO(
    val id: String,
    val network: NetworkInformationVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val apr: Double?,
    val stakedToken: FungibleTokenInformationVO,
    val stakedAmountDecimal: BigDecimal,
    val stakedAmount: String,
    val tokenAmountDecimal: BigDecimal,
    val tokenAmount: String,
    val rewardTokens: List<FungibleTokenInformationVO>,
    val exitPositionSupported: Boolean,
    val marketType: String = "farming",
    val expired: Boolean,
    val market: FarmingMarketVO
)