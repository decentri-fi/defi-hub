package io.defitrack.market.farming.vo

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigInteger

data class FarmingPositionVO(
    val id: String,
    val network: NetworkVO,
    val protocol: ProtocolVO,
    val dollarValue: Double,
    val name: String,
    val vaultType: String,
    val apr: Double?,
    val stakedToken: FungibleToken,
    val amount: Double,
    val nativeAmount: BigInteger,
    val rewardTokens: List<FungibleToken>,
    val exitPositionSupported: Boolean,
    val marketType: String = "farming"
)