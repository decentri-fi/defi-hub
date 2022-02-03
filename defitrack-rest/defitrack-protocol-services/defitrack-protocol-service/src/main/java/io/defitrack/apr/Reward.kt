package io.defitrack.apr

import io.defitrack.common.network.Network
import io.defitrack.protocol.staking.TokenType
import java.math.BigDecimal

class Reward(
    val address: String,
    val network: Network,
    val amount: BigDecimal,
    val tokenType: TokenType,
    val blocksPerSecond: Int = 1
)